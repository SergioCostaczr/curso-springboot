# Spring Security — Anotações do Módulo

## Visão Geral

Spring Security é o framework de segurança do ecossistema Spring, responsável por autenticação (quem é o usuário) e autorização (o que ele pode fazer). As principais anotações de configuração são:

- `@EnableWebSecurity` — ativa o mecanismo de segurança web
- `@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)` — habilita o uso de anotações de segurança diretamente nos controllers e services

---

## SecurityFilterChain

O bean `SecurityFilterChain` é onde toda a configuração de acesso é centralizada.

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(configurer -> configurer.loginPage("/login").permitAll())
        .httpBasic(Customizer.withDefaults())
        .authorizeHttpRequests(authorize -> {
            authorize.requestMatchers("/teste").permitAll();
            authorize.requestMatchers("/login/**").permitAll();
            authorize.requestMatchers(HttpMethod.POST, "/usuarios/**").permitAll();
            authorize.anyRequest().authenticated(); // qualquer outro endpoint exige autenticação
        })
        .build();
}
```

**Pontos importantes:**
- `permitAll()` libera o endpoint para acesso sem autenticação
- `authenticated()` exige que o usuário esteja logado (sem exigir role específica)
- Métodos como `hasRole()`, `hasAuthority()`, `hasAnyRole()` permitem restrição por perfil
- `.csrf(AbstractHttpConfigurer::disable)` desabilita proteção CSRF (comum em APIs REST)

---

## Autorização por Role/Authority

Pode ser feita de duas formas:

### 1. Diretamente no `SecurityFilterChain`
```java
authorize.requestMatchers(HttpMethod.POST, "/autores/**").hasRole("ADMIN");
authorize.requestMatchers(HttpMethod.GET, "/autores/**").hasAnyRole("USER", "ADMIN");
authorize.requestMatchers(HttpMethod.POST, "/autores/**").hasAuthority("CADASTRAR_USUARIO");
```

### 2. Via anotação no Controller (recomendado — mais granular)
```java
@PreAuthorize("hasRole('GERENTE')")
public ResponseEntity<?> salvar(@RequestBody @Valid AutorDTO dto) {
    // ...
}
```

> **Nota:** Para usar `@PreAuthorize`, é necessário ter `@EnableMethodSecurity` na classe de configuração.

---

## PasswordEncoder

Sempre use um `PasswordEncoder` para armazenar senhas com hash seguro:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10); // fator de custo 10
}
```

---

## UserDetailsService Customizado

Em vez de usar usuários em memória (`InMemoryUserDetailsManager`), foi criado um serviço customizado que busca o usuário no banco de dados:

```java
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioService service;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = service.obterPorLogin(login);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario nao encontrado");
        }

        return User.builder()
                .username(usuario.getLogin())
                .password(usuario.getSenha())
                .roles(usuario.getRoles().toArray(new String[0]))
                .build();
    }
}
```

E registrado como bean na configuração:

```java
@Bean
public UserDetailsService userDetailsService(UsuarioService usuarioService) {
    return new CustomUserDetailsService(usuarioService);
}
```

---

## Obtendo o Usuário Logado

### Forma 1 — Via parâmetro `Authentication` no Controller

```java
public ResponseEntity<?> salvar(@RequestBody @Valid AutorDTO dto, Authentication authentication) {
    UserDetails usuarioLogado = (UserDetails) authentication.getPrincipal();
    Usuario usuario = usuarioService.obterPorLogin(usuarioLogado.getUsername());
}
```

### Forma 2 — Via `SecurityService` (recomendado — reaproveitável em qualquer camada)

```java
@Component
@RequiredArgsConstructor
public class SecurityService {

    private final UsuarioService usuarioService;

    public Usuario obterUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String login = userDetails.getUsername();
        return usuarioService.obterPorLogin(login);
    }
}
```

**Uso no Service de negócio:**

```java
private final SecurityService securityService;

public Autor salvar(Autor autor) {
    validator.validar(autor);
    Usuario usuario = securityService.obterUsuarioLogado(); // busca o usuário autenticado
    autor.setUsuario(usuario);                              // vincula ao autor
    return autorRepository.save(autor);                    // persiste no banco
}
```

> Dessa forma, ao salvar um autor, ele é automaticamente associado ao usuário que está logado no momento da requisição, sem precisar passar o usuário manualmente via request.

---

## Fluxo Resumido

```
Requisição HTTP
    ↓
SecurityFilterChain (verifica autenticação/autorização)
    ↓
Controller (@PreAuthorize verifica role)
    ↓
Service (SecurityService.obterUsuarioLogado() busca usuário no contexto)
    ↓
Repository (persiste com o usuário vinculado)
```

---

## Conceitos-Chave

| Conceito | Descrição |
|---|---|
| `Authentication` | Objeto que representa o usuário autenticado no contexto da requisição |
| `SecurityContextHolder` | Armazena o contexto de segurança da thread atual |
| `UserDetails` | Interface que o Spring usa para representar um usuário |
| `UserDetailsService` | Interface para carregar o usuário pelo login |
| `PasswordEncoder` | Interface para codificar e verificar senhas |
| `@PreAuthorize` | Anotação para proteger métodos por expressão de segurança |
| `hasRole()` | Verifica se o usuário possui determinada role (adiciona prefixo `ROLE_` automaticamente) |
| `hasAuthority()` | Verifica se o usuário possui determinada authority (sem prefixo) |
