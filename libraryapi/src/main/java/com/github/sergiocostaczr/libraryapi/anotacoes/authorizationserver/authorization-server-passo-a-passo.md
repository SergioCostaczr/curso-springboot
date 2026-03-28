# Construindo um Authorization Server do Zero

Este documento acompanha o desenvolvimento de um Authorization Server completo, na ordem em que faz mais sentido construí-lo. Cada passo explica o que está sendo criado, por que vem nesse momento e como se conecta com o restante.

---

## Passo 1 — Entidade Client

O Authorization Server precisa saber quais aplicações estão autorizadas a pedir tokens. Antes de qualquer configuração de segurança, é necessário um lugar para guardar essa informação. Começa-se pela entidade JPA que vai representar um client no banco.

```java
@Entity
@Table(name = "client")
@Data
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id", unique = true, nullable = false)
    private String clientId;

    @Column(name = "client_secret", nullable = false)
    private String clientSecret;

    @Column(name = "redirect_uri")
    private String redirectUri;

    @Column(nullable = false)
    private String scope;
}
```

`clientId` é o identificador público — funciona como o username da aplicação. Qualquer um pode saber qual é o `clientId`. Sozinho ele não autoriza nada.

`clientSecret` é a senha da aplicação. Será armazenado com BCrypt, nunca em texto puro. É o que prova que a aplicação é quem diz ser.

`redirectUri` só é usado no fluxo Authorization Code. Após o usuário fazer login, o Authorization Server vai redirecionar o browser para essa URL com um código de autorização. Guardar esse valor no banco impede que uma aplicação maliciosa use uma URL diferente para interceptar o código.

`scope` define o que esse client pode solicitar — por exemplo `GERENTE` ou `OPERADOR`. O valor vai ficar gravado dentro do JWT emitido, e o Resource Server pode checar não só se o token é válido, mas se quem o pediu tinha permissão para aquele escopo específico.

---

## Passo 2 — Repositório e serviço do Client

Com a entidade definida, é necessário uma forma de acessá-la no banco. O repositório JPA e o serviço são criados juntos porque o serviço já encapsula a única regra de negócio importante neste momento: criptografar o `clientSecret` antes de salvar.

```java
// ClientRepository.java
public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByClientId(String clientId);
}
```

```java
// ClientService.java
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository repository;
    private final PasswordEncoder passwordEncoder;

    public Client obterPorClientId(String clientId) {
        return repository.findByClientId(clientId).orElse(null);
    }

    public void salvar(Client client) {
        String secretCriptografado = passwordEncoder.encode(client.getClientSecret());
        client.setClientSecret(secretCriptografado);
        repository.save(client);
    }
}
```

O motivo de criptografar o secret é o mesmo motivo de criptografar senhas de usuário: se o banco for comprometido, os secrets originais não ficam expostos. O Spring Authorization Server, ao validar um client, usa o `PasswordEncoder` para comparar o secret da requisição com o hash guardado no banco — exatamente como funciona com senhas de usuário.

Note que o `PasswordEncoder` é injetado aqui, mas ainda não foi definido como Bean. Ele será definido na configuração do Authorization Server no Passo 5. Em Spring, a injeção é resolvida em tempo de execução, então a ordem de definição dos Beans não importa.

---

## Passo 3 — Configurações de TokenSettings e ClientSettings

Antes de criar o filtro do Authorization Server, é útil definir as configurações de token e de client como Beans. Elas serão injetadas no `CustomRegisteredClientRepository` (Passo 4) e aplicadas a todos os clients.

```java
@Bean
public TokenSettings tokenSettings() {
    return TokenSettings.builder()
        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
        .accessTokenTimeToLive(Duration.ofMinutes(60))
        .refreshTokenTimeToLive(Duration.ofMinutes(90))
        .build();
}

@Bean
public ClientSettings clientSettings() {
    return ClientSettings.builder()
        .requireAuthorizationConsent(false)
        .build();
}
```

`SELF_CONTAINED` define que o token será um JWT. O token carrega todas as informações necessárias dentro de si — quem é o usuário, quais são as permissões, quando expira. O Resource Server consegue validá-lo sem precisar consultar o Authorization Server a cada requisição. A alternativa seria um token opaco, que é apenas um identificador aleatório e exigiria uma consulta ao Authorization Server em cada request, gerando latência e acoplamento.

`requireAuthorizationConsent(false)` desabilita a tela de consentimento — aquela tela do Google que pergunta "Você autoriza esse app a acessar seus dados?". Faz sentido em aplicações públicas onde um usuário está autorizando um app de terceiro. Em uma API interna, isso seria apenas fricção desnecessária.

---

## Passo 4 — CustomRegisteredClientRepository

O Spring Authorization Server não trabalha com a entidade `Client` diretamente. Ele usa um objeto próprio chamado `RegisteredClient` e uma interface chamada `RegisteredClientRepository` para buscá-lo. Ao implementar essa interface com uma busca no banco, substitui-se o repositório padrão em memória por um persistente.

```java
@Component
@RequiredArgsConstructor
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    private final ClientService clientService;
    private final TokenSettings tokenSettings;
    private final ClientSettings clientSettings;

    @Override
    public void save(RegisteredClient registeredClient) {
        // Cadastro é gerenciado pela entidade Client e ClientService
    }

    @Override
    public RegisteredClient findById(String id) {
        return null; // Não utilizado no fluxo principal
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        Client client = clientService.obterPorClientId(clientId);

        if (client == null) return null;

        return RegisteredClient
            .withId(client.getId().toString())
            .clientId(client.getClientId())
            .clientSecret(client.getClientSecret())
            .redirectUri(client.getRedirectUri())
            .scope(client.getScope())
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .tokenSettings(tokenSettings)
            .clientSettings(clientSettings)
            .build();
    }
}
```

O método `findByClientId` é chamado pelo Authorization Server toda vez que uma aplicação tenta se autenticar. Ele busca o `Client` no banco, constrói e devolve o `RegisteredClient` equivalente.

`CLIENT_SECRET_BASIC` define como o client prova sua identidade: enviando `clientId` e `clientSecret` no header `Authorization` no formato Base64 — `Authorization: Basic base64(clientId:clientSecret)`. É o mesmo formato do HTTP Basic Auth, amplamente suportado por ferramentas como Postman.

Os três `authorizationGrantType` autorizam esse client a usar os três fluxos disponíveis: Authorization Code para fluxos com usuário, Client Credentials para comunicação máquina a máquina, e Refresh Token para renovar o access_token sem novo login.

---

## Passo 5 — Par de chaves RSA e JWK Source

Os JWTs emitidos pelo Authorization Server precisam ser assinados para que o Resource Server possa verificar sua autenticidade. A solução é um par de chaves RSA: a chave privada assina os tokens, e a chave pública é disponibilizada para qualquer serviço que precise verificá-los.

```java
@Bean
public JWKSource<SecurityContext> jwkSource() throws Exception {
    RSAKey rsaKey = gerarChaveRSA();
    JWKSet jwkSet = new JWKSet(rsaKey);
    return new ImmutableJWKSet<>(jwkSet);
}

private RSAKey gerarChaveRSA() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    KeyPair keyPair = generator.generateKeyPair();

    RSAPublicKey chavePublica = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey chavePrivada = (RSAPrivateKey) keyPair.getPrivate();

    return new RSAKey.Builder(chavePublica)
        .privateKey(chavePrivada)
        .keyID(UUID.randomUUID().toString())
        .build();
}
```

O motivo de usar RSA em vez de uma chave simétrica é a distribuição segura. Com uma chave simétrica (HMAC), a mesma chave assina e verifica — o que significa que todo Resource Server precisaria da chave secreta, aumentando o risco. Com RSA, a chave privada nunca sai do Authorization Server, e a chave pública pode ser distribuída livremente. Quem tem a chave pública consegue verificar que um token é legítimo, mas não consegue criar novos tokens.

O `keyID` é um identificador único gravado no header do JWT. Quando o Resource Server vai verificar um token, ele lê o `keyID` e sabe qual chave do JWK Set usar — o que permite ter múltiplas chaves válidas simultaneamente durante uma rotação de chaves.

O JWK Set é a coleção de chaves. O Spring vai expô-lo automaticamente no endpoint `GET /oauth2/jwks`, acessível por qualquer Resource Server.

> Em produção, o par de chaves não pode ser gerado em memória a cada restart. Tokens emitidos antes do restart ficariam inválidos pois a chave que os assinou não existiria mais. A chave RSA precisa ser persistida em banco de dados, em um cofre de segredos (HashiCorp Vault, AWS Secrets Manager) ou lida de um arquivo protegido na inicialização.

---

## Passo 6 — PasswordEncoder e JwtDecoder

Dois Beans de suporte são necessários antes de montar o filtro principal.

O `PasswordEncoder` é necessário para que o Authorization Server possa verificar o `clientSecret` da requisição contra o hash armazenado no banco, e também para verificar senhas de usuários no formulário de login.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
}
```

O `JwtDecoder` é necessário porque o endpoint `/userinfo` do OIDC recebe um JWT e precisa validá-lo antes de responder. Ou seja, nesse endpoint específico, o Authorization Server age como Resource Server — ele recebe e valida um token, não emite um.

```java
@Bean
public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
}
```

Ele usa o mesmo `jwkSource` definido no passo anterior, o que significa que tem acesso à chave pública para verificar assinaturas.

---

## Passo 7 — SecurityFilterChain do Authorization Server

Com todos os componentes prontos, o passo central é configurar o filtro que vai expor os endpoints do Authorization Server e definir as regras de segurança deles.

```java
@Bean
@Order(1)
public SecurityFilterChain authServerFilterChain(HttpSecurity http) throws Exception {

    OAuth2AuthorizationServerConfigurer configurer = new OAuth2AuthorizationServerConfigurer();

    http.with(configurer, server ->
        server.oidc(Customizer.withDefaults())
    );

    http.oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()));
    http.formLogin(c -> c.loginPage("/login"));

    return http.build();
}
```

O `@Order(1)` garante que este filtro seja processado antes do segundo `SecurityFilterChain` (que vai ser criado no Passo 9). Quando uma requisição chega em `/oauth2/token` ou `/oauth2/authorize`, este filtro a intercepta primeiro.

A linha `http.with(configurer, ...)` é o coração de tudo. Ao aplicar o `OAuth2AuthorizationServerConfigurer`, o Spring registra automaticamente os seguintes endpoints:

```
POST /oauth2/token       → emite tokens (todos os grant types)
GET  /oauth2/authorize   → inicia o fluxo Authorization Code
GET  /oauth2/jwks        → expõe a chave pública RSA
GET  /userinfo           → retorna dados do usuário (OIDC)
GET  /.well-known/...    → metadados do servidor (discovery)
```

O `oidc(Customizer.withDefaults())` habilita o OpenID Connect. Além do `access_token`, o servidor passa a poder emitir um `id_token` com informações do usuário — nome, email, etc. — e o endpoint `/userinfo` fica ativo.

O `oauth2ResourceServer(rs -> rs.jwt(...))` habilita a validação de JWTs neste filtro, necessária para o endpoint `/userinfo` funcionar.

O `formLogin(c -> c.loginPage("/login"))` define para onde redirecionar o usuário quando ele tenta iniciar o fluxo Authorization Code sem estar autenticado.

---

## Passo 8 — Autenticação do usuário humano

O Authorization Server já sabe autenticar clients (aplicações). Agora é necessário ensinar a aplicação a autenticar usuários humanos, que é necessário no fluxo Authorization Code quando alguém precisa fazer login.

O objetivo é que, independente de como o usuário se autenticou — formulário ou Google — o `SecurityContext` sempre contenha a mesma representação padronizada do usuário, com acesso ao `Usuario` do banco.

### CustomAuthentication

É a representação unificada do usuário autenticado. Implementa a interface `Authentication` e carrega a entidade `Usuario`:

```java
@RequiredArgsConstructor
@Getter
public class CustomAuthentication implements Authentication {

    private final Usuario usuario;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return usuario.getRoles()
            .stream()
            .map(SimpleGrantedAuthority::new)
            .toList();
    }

    @Override
    public Object getPrincipal()        { return usuario; }

    @Override
    public Object getCredentials()      { return null; }

    @Override
    public Object getDetails()          { return usuario; }

    @Override
    public boolean isAuthenticated()    { return true; }

    @Override
    public void setAuthenticated(boolean b) { }

    @Override
    public String getName()             { return usuario.getLogin(); }
}
```

### CustomAuthenticationProvider

É o componente que verifica login e senha no fluxo de formulário e Basic Auth. O Spring o consulta quando precisa autenticar um `UsernamePasswordAuthenticationToken`:

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

        Usuario usuario = usuarioService.obterPorLogin(login);

        if (usuario == null) throw new UsernameNotFoundException("Usuário e/ou senha incorretos");

        boolean senhasBatem = passwordEncoder.matches(senhaDigitada, usuario.getSenha());

        if (senhasBatem) return new CustomAuthentication(usuario);

        throw new UsernameNotFoundException("Usuário e/ou senha incorretos");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
```

O método `supports` é a forma que o Spring usa para descobrir qual `AuthenticationProvider` chamar. Ao retornar `true` para `UsernamePasswordAuthenticationToken`, este provider declara que sabe lidar com esse tipo de autenticação.

### LoginSocialSuccessHandler

Quando o usuário faz login pelo Google, o Spring cria um `OAuth2AuthenticationToken` com as informações do Google. Este handler intercepta o momento logo após a autenticação, extrai o email, busca ou cadastra o usuário no banco, e substitui o token do Google pelo `CustomAuthentication`:

```java
@Component
@RequiredArgsConstructor
public class LoginSocialSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String email = token.getPrincipal().getAttribute("email");

        Usuario usuario = usuarioService.obterPorEmail(email);
        if (usuario == null) usuario = cadastrarUsuario(email);

        // Substitui o token do Google pelo CustomAuthentication da aplicação
        authentication = new CustomAuthentication(usuario);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Continua o fluxo — redireciona para onde o usuário queria ir
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private Usuario cadastrarUsuario(String email) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setLogin(email.substring(0, email.indexOf("@")));
        usuario.setSenha(passwordEncoder.encode("senha-padrao"));
        usuario.setRoles(List.of("OPERADOR"));
        usuarioService.salvar(usuario);
        return usuario;
    }
}
```

O motivo de herdar de `SavedRequestAwareAuthenticationSuccessHandler` em vez de implementar `AuthenticationSuccessHandler` diretamente é o comportamento de redirecionamento. Quando o usuário tentava acessar um endpoint protegido antes de estar logado, o Spring salva a requisição original. Ao herdar desse handler, ao chamar `super.onAuthenticationSuccess(...)`, o Spring automaticamente redireciona o usuário de volta para onde ele queria ir originalmente.

---

## Passo 9 — SecurityFilterChain da API (Resource Server)

O segundo `SecurityFilterChain` protege os endpoints da API. Com ordem padrão (maior que 1), é processado após o filtro do Authorization Server.

```java
@Bean
public SecurityFilterChain apiFilterChain(HttpSecurity http,
                                          LoginSocialSuccessHandler handler) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(c -> c.loginPage("/login").permitAll())
        .httpBasic(Customizer.withDefaults())
        .authorizeHttpRequests(authorize -> {
            authorize.requestMatchers("/login/**").permitAll();
            authorize.requestMatchers(HttpMethod.POST, "/usuarios/**").permitAll();
            authorize.anyRequest().authenticated();
        })
        .oauth2Login(oauth2 -> {
            oauth2.loginPage("/login");
            oauth2.successHandler(handler);
        })
        .oauth2ResourceServer(oauth2RS -> Customizer.withDefaults())
        .build();
}
```

O `oauth2ResourceServer` configura este filtro para interceptar o header `Authorization: Bearer eyJ...` nas requisições à API. Ele usa o `JwtDecoder` para validar o token e, se for válido, popula o `SecurityContext` com as permissões extraídas do JWT.

---

## Passo 10 — Ajustes de prefixo nas permissões

Dois últimos Beans resolvem um comportamento padrão do Spring que conflitaria com o projeto.

```java
@Bean
public GrantedAuthorityDefaults grantedAuthorityDefaults() {
    return new GrantedAuthorityDefaults(""); // Remove prefixo ROLE_
}

@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    authoritiesConverter.setAuthorityPrefix(""); // Remove prefixo SCOPE_

    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    return converter;
}
```

Por padrão, o Spring adiciona `ROLE_` às roles e `SCOPE_` aos escopos lidos do JWT. Como as permissões no projeto foram definidas sem prefixo — `GERENTE`, `OPERADOR`, não `ROLE_GERENTE` ou `SCOPE_GERENTE` — as verificações de autorização nos controllers falhariam. Esses dois Beans removem os prefixos para que tudo se alinhe.

---

## Visão geral do que foi construído

```
BANCO DE DADOS
┌──────────────────────────────────────────────────────┐
│  tabela: client          tabela: usuario              │
│  - client_id             - login                      │
│  - client_secret (hash)  - senha (hash)               │
│  - redirect_uri          - roles                      │
│  - scope                                              │
└────────────┬─────────────────────┬────────────────────┘
             │                     │
             ▼                     ▼
    ClientService           UsuarioService
             │                     │
             │              ┌──────┴───────────────────┐
             │              │  CustomAuthenticationProvider (form/basic)
             │              │  LoginSocialSuccessHandler (Google)
             │              └──────┬───────────────────┘
             │                     │
             ▼                     ▼
  CustomRegisteredClientRepository  CustomAuthentication
             │                     │
             ▼                     ▼
  ┌──────────────────────────────────────────────────┐
  │       Authorization Server  (@Order 1)            │
  │                                                   │
  │  POST /oauth2/token    → emite JWT assinado       │
  │  GET  /oauth2/authorize → inicia fluxo auth code  │
  │  GET  /oauth2/jwks     → expõe chave pública RSA  │
  │  GET  /userinfo        → dados do usuário (OIDC)  │
  └─────────────────────┬────────────────────────────┘
                        │  JWT assinado com chave privada RSA
                        ▼
              Client recebe o JWT
                        │
                        ▼  Authorization: Bearer eyJ...
  ┌──────────────────────────────────────────────────┐
  │       Resource Server (sua API)                   │
  │                                                   │
  │  Valida JWT com chave pública de /oauth2/jwks     │
  │  Popula SecurityContext com permissões do token   │
  │  Libera ou rejeita o acesso ao endpoint           │
  └──────────────────────────────────────────────────┘
```

---

## Ordem de desenvolvimento resumida

```
Passo 1  →  Entidade Client (banco de dados)
Passo 2  →  ClientRepository + ClientService
Passo 3  →  TokenSettings + ClientSettings (Beans de configuração)
Passo 4  →  CustomRegisteredClientRepository (ponte Client → RegisteredClient)
Passo 5  →  Par de chaves RSA + JWKSource (assinatura dos JWTs)
Passo 6  →  PasswordEncoder + JwtDecoder (suporte)
Passo 7  →  SecurityFilterChain do Authorization Server (@Order 1)
Passo 8  →  CustomAuthentication + CustomAuthenticationProvider + LoginSocialSuccessHandler
Passo 9  →  SecurityFilterChain da API (Resource Server)
Passo 10 →  GrantedAuthorityDefaults + JwtAuthenticationConverter (ajuste de prefixos)
```
