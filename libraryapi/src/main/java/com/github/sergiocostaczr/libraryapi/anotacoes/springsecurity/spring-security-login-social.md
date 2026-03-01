# Spring Security — Login Social e Autenticação Unificada

## Visão Geral

Nesta seção o objetivo foi unificar **todas as formas de autenticação** (form login, HTTP Basic e OAuth2/Google) em um único objeto de autenticação customizado — o `CustomAuthentication`. Independente de como o usuário entrou, o sistema sempre termina com uma instância dessa classe no `SecurityContext`.

```
Login com senha  ──→ CustomAuthenticationProvider ──┐
                                                      ├──→ CustomAuthentication (sempre)
Login com Google ──→ LoginSocialSuccessHandler ───────┘
```

---

## 1. CustomAuthentication

É a **representação unificada do usuário autenticado** dentro da aplicação. Implementa a interface `Authentication` do Spring Security.

```java
@RequiredArgsConstructor
@Getter
public class CustomAuthentication implements Authentication {

    private final Usuario usuario; // entidade do banco de dados

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.usuario.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .toList();
    }

    @Override
    public Object getPrincipal() {
        return usuario; // retorna a entidade Usuario diretamente
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public String getName() {
        return usuario.getLogin();
    }

    // getCredentials() e getDetails() retornam null — senha não precisa ficar no contexto
}
```

**Por que isso é útil?**
- `getPrincipal()` retorna a entidade `Usuario` diretamente, então em qualquer ponto do código dá para fazer o cast sem precisar buscar no banco novamente.
- Tanto o login por senha quanto o login pelo Google produzem essa mesma classe no final.

---

## 2. CustomAuthenticationProvider

Responsável por **validar login e senha** quando o usuário tenta autenticar via form ou HTTP Basic. É chamado automaticamente pelo Spring quando detecta um `UsernamePasswordAuthenticationToken`.

```java
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String login = authentication.getName();
        String senhaDigitada = authentication.getCredentials().toString();

        // 1. Busca o usuário
        Usuario usuario = usuarioService.obterPorLogin(login);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario e/ou senha incorretos!");
        }

        // 2. Compara a senha digitada com o hash no banco
        boolean senhasBatem = passwordEncoder.matches(senhaDigitada, usuario.getSenha());

        // 3. Se correto, retorna CustomAuthentication
        if (senhasBatem) {
            return new CustomAuthentication(usuario);
        }

        throw new UsernameNotFoundException("Usuario e/ou senha incorretos!");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // Diz ao Spring: "eu só processo UsernamePasswordAuthenticationToken"
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
```

**Fluxo:**
```
Usuário digita login + senha
        ↓
Spring cria UsernamePasswordAuthenticationToken
        ↓
CustomAuthenticationProvider.authenticate() é chamado
        ↓
Valida senha com passwordEncoder.matches()
        ↓
Retorna CustomAuthentication(usuario) → vai pro SecurityContext
```

> **Nota:** O método `supports()` é essencial — sem ele o Spring não sabe que esse provider deve ser usado para autenticações de usuário/senha.

---

## 3. LoginSocialSuccessHandler — OAuth2 com Google

O Google **valida o token OAuth2 por conta própria**. Quando a validação termina, o Spring já cria um `OAuth2AuthenticationToken` automaticamente. O `LoginSocialSuccessHandler` é chamado logo após esse momento.

```java
@Component
@RequiredArgsConstructor
public class LoginSocialSucessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final String SENHA_PADRAO = "123";

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        // 1. Recupera o token do Google
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        // 2. Extrai o email do perfil do Google
        String email = oAuth2User.getAttribute("email");

        // 3. Busca ou cria o usuário na base da aplicação
        Usuario user = usuarioService.obterPorEmail(email);
        if (user == null) {
            user = cadastrarUsuarioNaBase(email);
        }

        // 4. Substitui o token do Google pelo nosso CustomAuthentication
        authentication = new CustomAuthentication(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 5. Continua o fluxo normal (redireciona, etc.)
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private Usuario cadastrarUsuarioNaBase(String email) {
        Usuario user = new Usuario();
        user.setEmail(email);
        user.setLogin(email.substring(0, email.indexOf("@"))); // parte antes do @
        user.setSenha(SENHA_PADRAO);
        user.setRoles(List.of("OPERADOR"));
        usuarioService.salvar(user);
        return user;
    }
}
```

**Fluxo:**
```
Usuário clica em "Entrar com Google"
        ↓
Google valida o token OAuth2
        ↓
Spring cria OAuth2AuthenticationToken automaticamente
        ↓
LoginSocialSuccessHandler.onAuthenticationSuccess() é chamado
        ↓
Extrai email do perfil do Google
        ↓
Busca no banco — se não existe, cadastra com role OPERADOR
        ↓
Substitui o token do Google por CustomAuthentication(usuario)
        ↓
SecurityContextHolder atualizado com nosso token unificado
```

> **Importante:** O `SecurityContextHolder.getContext().setAuthentication(authentication)` é o passo que substitui o token do Google pelo nosso customizado. Sem isso, o principal seria um `OAuth2User` do Google e não nossa entidade `Usuario`.

---

## 4. SecurityConfiguration — Configuração Atualizada

### OAuth2 adicionado ao `SecurityFilterChain`

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                LoginSocialSucessHandler loginSocialSucessHandler) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(configurer -> configurer.loginPage("/login").permitAll())
        .httpBasic(Customizer.withDefaults())
        .authorizeHttpRequests(authorize -> {
            authorize.requestMatchers("/teste", "/login/**").permitAll();
            authorize.requestMatchers(HttpMethod.POST, "/usuarios/**").permitAll();
            authorize.anyRequest().authenticated();
        })
        .oauth2Login(oauth2 -> oauth2
            .loginPage("/login")                          // usa nossa página de login customizada
            .successHandler(loginSocialSucessHandler)     // nosso handler pós-autenticação
        )
        .build();
}
```

### Removendo o prefixo `ROLE_`

Por padrão, o Spring adiciona o prefixo `ROLE_` nas authorities ao usar `hasRole()`. Para remover esse comportamento e trabalhar com os nomes das roles diretamente:

```java
@Bean
public GrantedAuthorityDefaults grantedAuthorityDefaults() {
    return new GrantedAuthorityDefaults(""); // string vazia remove o prefixo ROLE_
}
```

> Com isso, uma role chamada `"GERENTE"` pode ser usada como `hasRole('GERENTE')` sem precisar de `"ROLE_GERENTE"` na base.

### `UserDetailsService` desativado

O bean `userDetailsService()` foi comentado (`//@Bean`) porque o `CustomAuthenticationProvider` assumiu completamente a responsabilidade de autenticar usuários por senha. Os dois não devem coexistir como beans ativos.

---

## Resumo: Três formas de autenticar, um único resultado

| Tipo de Login | Quem processa | Resultado no SecurityContext |
|---|---|---|
| Form / HTTP Basic | `CustomAuthenticationProvider` | `CustomAuthentication` |
| Google (OAuth2) | Google + `LoginSocialSuccessHandler` | `CustomAuthentication` |

Como `getPrincipal()` sempre retorna a entidade `Usuario`, o `SecurityService` continua funcionando sem alterações para qualquer tipo de login:

```java
public Usuario obterUsuarioLogado() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    // Agora o cast vai direto para Usuario, sem precisar buscar no banco!
    return (Usuario) authentication.getPrincipal();
}
```
