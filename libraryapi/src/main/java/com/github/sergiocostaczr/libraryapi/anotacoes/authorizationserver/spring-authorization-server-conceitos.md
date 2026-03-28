# Spring Authorization Server — Conceitos

## 1. Os três personagens do OAuth2

Todo fluxo OAuth2 envolve três participantes. Entender o papel de cada um é o ponto de partida.

**Authorization Server** é o único autorizado a emitir tokens. Ninguém mais pode gerar um token válido, porque só ele tem a chave privada RSA que assina os tokens. É o cartório da aplicação.

**Resource Server** é a sua API — os endpoints `/autores`, `/livros`, etc. Ele não emite tokens, apenas os valida. Quando chega uma requisição com um JWT, ele verifica se a assinatura é válida e se o token não expirou. Se tudo estiver certo, libera o acesso.

**Client** é qualquer aplicação que quer consumir a API: um frontend, o Postman, um serviço externo. Para pedir um token, o client precisa estar previamente cadastrado no Authorization Server.

No projeto, Authorization Server e Resource Server vivem na mesma aplicação Spring Boot — são dois `SecurityFilterChain` diferentes, com responsabilidades separadas, convivendo no mesmo processo.

---

## 2. Client e usuário — qual a diferença

Essa é uma das confusões mais comuns. Client e usuário são entidades completamente separadas, com propósitos diferentes.

O **client** é a aplicação — o Postman, um frontend, um serviço. Ele tem `clientId` e `clientSecret`, guardados na tabela `Client` do banco. O client se autentica para provar ao Authorization Server que ele é uma aplicação registrada e autorizada a pedir tokens.

O **usuário** é a pessoa — alguém com login e senha, guardado na tabela `Usuario` do banco. O usuário se autentica para provar que é uma pessoa real com permissões específicas dentro do sistema.

A diferença fica concreta nos grant types. No **Client Credentials**, só o client se autentica — não existe usuário no fluxo. O token representa a própria aplicação. No **Authorization Code**, os dois se autenticam em momentos diferentes: o client se identifica na requisição, e depois o usuário humano faz login na tela de formulário. O token final representa o usuário.

O client é a porta de entrada. O usuário é quem passa por essa porta. Em Client Credentials, a porta abre sozinha. Em Authorization Code, a porta precisa estar desbloqueada e uma pessoa precisa passar por ela.

---

## 3. RegisteredClient — a representação do client no Spring

O Spring Authorization Server não trabalha diretamente com a entidade `Client` do banco. Ele usa um objeto próprio chamado `RegisteredClient`. O `CustomRegisteredClientRepository` é a ponte entre os dois — ele busca o `Client` no banco e constrói um `RegisteredClient` equivalente.

```java
return RegisteredClient
    .withId(client.getId().toString())
    .clientId(client.getClientId())
    .clientSecret(client.getClientSecret())
    .redirectUri(client.getRedirectURi())
    .scope(client.getScope())
    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
    .tokenSettings(tokenSettings)
    .clientSettings(clientSettings)
    .build();
```

**`clientAuthenticationMethod(CLIENT_SECRET_BASIC)`** — define como o client prova sua identidade. Com `CLIENT_SECRET_BASIC`, ele envia `clientId` e `clientSecret` no header `Authorization` no formato Base64: `Authorization: Basic base64(clientId:clientSecret)`.

**`authorizationGrantType`** — quais fluxos esse client tem permissão de usar. Um client pode suportar mais de um ao mesmo tempo.

**`redirectUri`** — no fluxo Authorization Code, após o login, o Authorization Server só pode redirecionar para URLs cadastradas aqui. Sem isso, uma aplicação maliciosa poderia usar outro redirect para capturar o código de autorização.

**`scope`** — o que o client pode solicitar. Os escopos aprovados ficam gravados no JWT. O Resource Server pode checar não só se o token é válido, mas se quem o pediu tinha permissão para aquele escopo.

**`tokenSettings`**:

```java
@Bean
public TokenSettings tokenSettings() {
    return TokenSettings.builder()
        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED) // formato JWT
        .accessTokenTimeToLive(Duration.ofMinutes(60))
        .refreshTokenTimeToLive(Duration.ofMinutes(90))
        .build();
}
```

`SELF_CONTAINED` significa que o token carrega todas as informações dentro de si mesmo, no formato JWT. O Resource Server consegue validá-lo sem precisar consultar o Authorization Server a cada requisição. A alternativa seria um token opaco, que seria apenas um identificador — e o Resource Server teria que fazer uma chamada ao Authorization Server para descobrir o que ele significa.

**`clientSettings`**:

```java
@Bean
public ClientSettings clientSettings() {
    return ClientSettings.builder()
        .requireAuthorizationConsent(false)
        .build();
}
```

`requireAuthorizationConsent(false)` desabilita a tela de consentimento — aquela tela do Google que pergunta "Você autoriza esse app a acessar seus dados?". Em uma API interna, isso não faz sentido.

---

## 4. JWT — o formato do token

O token emitido é um JWT com três partes separadas por ponto:

```
eyJhbGciOiJSUzI1NiJ9  .  eyJzdWIiOiJ1c2VyMSIsInNjb3BlIjoiR0VSRU5URSIsImV4cCI6MTcxMH0  .  assinatura
      [header]                                    [payload]                                    [signature]
```

O **header** indica o algoritmo de assinatura — no caso RSA com SHA-256 (`RS256`).

O **payload** contém as informações: quem é o usuário (`sub`), os escopos (`scope`), quando expira (`exp`), quem emitiu (`iss`). Essas informações podem ser lidas por qualquer um, pois o payload é apenas Base64 — não é criptografado.

A **signature** garante integridade. Ela é gerada assinando o header + payload com a chave privada RSA. Se alguém alterar qualquer dado do payload, a assinatura não vai mais bater e o token será rejeitado.

---

## 5. JWK — como a chave pública é distribuída

Para verificar a assinatura de um JWT, o Resource Server precisa da chave pública do Authorization Server. O JWK (JSON Web Key) é o formato padrão para representar essa chave em JSON. O Authorization Server a expõe automaticamente no endpoint `/oauth2/jwks`.

O par de chaves é gerado assim:

```java
@Bean
public JWKSource<SecurityContext> jwkSource() throws Exception {
    RSAKey rsaKey = gerarChaveRSA();
    return new ImmutableJWKSet<>(new JWKSet(rsaKey));
}

private RSAKey gerarChaveRSA() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair keyPair = gen.generateKeyPair();

    return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
        .privateKey((RSAPrivateKey) keyPair.getPrivate())
        .keyID(UUID.randomUUID().toString())
        .build();
}
```

A **chave privada** fica no Authorization Server e nunca sai dele. É usada para assinar tokens.

A **chave pública** é distribuída pelo endpoint `/oauth2/jwks`. Qualquer Resource Server pode baixá-la para verificar assinaturas. Quem tem a chave pública consegue verificar que um token é autêntico, mas não consegue criar novos tokens.

O **`keyID`** é um identificador único da chave. Ele fica gravado no header do JWT. Quando o Resource Server vai verificar um token, ele lê o `keyID` e sabe exatamente qual chave do JWK Set usar — útil quando há rotação de chaves.

> O par de chaves no projeto é gerado em memória a cada restart. Em produção, a chave RSA precisa ser persistida — em banco de dados ou em um cofre de segredos — para que tokens emitidos antes do restart continuem válidos.

---

## 6. A configuração do Authorization Server

```java
@Bean
@Order(1)
public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {

    OAuth2AuthorizationServerConfigurer configurer = new OAuth2AuthorizationServerConfigurer();

    http.with(configurer, server ->
        server.oidc(Customizer.withDefaults())
    );

    http.oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()));
    http.formLogin(c -> c.loginPage("/login"));

    return http.build();
}
```

**`@Order(1)`** — a aplicação tem dois `SecurityFilterChain`. O Spring precisa decidir qual processa cada requisição. Com `@Order(1)`, este filtro tem prioridade e intercepta requisições do Authorization Server antes que o outro filtro as veja.

**`http.with(configurer, ...)`** — registra automaticamente todos os endpoints do Authorization Server. Sem isso, esses endpoints não existiriam. Os principais são:

| Endpoint | Função |
|---|---|
| `POST /oauth2/token` | Emite tokens — todos os grant types passam por aqui |
| `GET /oauth2/authorize` | Inicia o fluxo Authorization Code |
| `GET /oauth2/jwks` | Expõe a chave pública RSA |
| `GET /userinfo` | Retorna dados do usuário autenticado (OIDC) |

**`oidc(Customizer.withDefaults())`** — habilita o OpenID Connect, uma camada em cima do OAuth2. Com OIDC, além do `access_token`, o servidor pode emitir um `id_token` com dados do usuário, e o endpoint `/userinfo` fica disponível.

**`oauth2ResourceServer(rs -> rs.jwt(...))`** — o endpoint `/userinfo` recebe um JWT e precisa validá-lo. Por isso o Authorization Server também age como Resource Server nesse ponto específico.

**`formLogin(c -> c.loginPage("/login"))`** — no fluxo Authorization Code, quando o usuário não está logado, o Authorization Server precisa redirecionar para uma tela de login. Essa linha define qual é essa página.

---

## 7. A autenticação do usuário humano

No fluxo Authorization Code existe um usuário humano que precisa fazer login. O objetivo do projeto é que, independente de como o usuário se autenticou — formulário ou Google — o `SecurityContext` sempre contenha um `CustomAuthentication` com o `Usuario` do banco.

```
Form Login   ──┐
Google OAuth ──┼──► CustomAuthentication(usuario) ──► SecurityContext
```

**`CustomAuthenticationProvider`** — verifica login e senha no fluxo de formulário e Basic Auth. Recebe um `UsernamePasswordAuthenticationToken`, busca o usuário no banco, compara a senha com BCrypt e, se bater, devolve um `CustomAuthentication`. O método `supports` diz ao Spring que este provider só sabe lidar com `UsernamePasswordAuthenticationToken`.

**`CustomAuthentication`** — implementa a interface `Authentication` e carrega o `Usuario` do banco. Expõe as roles dele como `GrantedAuthority`. Padroniza a representação do usuário autenticado em todo o sistema.

**`LoginSocialSuccessHandler`** — quando o Google termina a autenticação, o Spring cria um `OAuth2AuthenticationToken`. Este handler intercepta esse momento, extrai o email, busca ou cadastra o usuário no banco e substitui o token do Google pelo `CustomAuthentication`.

---

## 8. O Resource Server — validando tokens na API

O segundo `SecurityFilterChain`, com ordem padrão, protege os endpoints da API:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                               LoginSocialSucessHandler handler) throws Exception {
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

O `oauth2ResourceServer` configura este filtro para validar JWTs nas requisições que chegam à API. Quando uma requisição chega com `Authorization: Bearer eyJ...`, o filtro usa o `JwtDecoder` para validar o token e, se for válido, popula o `SecurityContext`.

Dois Beans adicionais ajustam como as permissões são lidas do JWT:

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

Por padrão, o Spring adiciona `ROLE_` às roles e `SCOPE_` aos escopos extraídos do JWT. Como as permissões no projeto foram definidas sem prefixo (ex: `GERENTE`, `OPERADOR`), esses dois Beans removem esses prefixos para que as verificações de autorização funcionem corretamente.

---

## 9. Grant Types

Grant type é o nome do fluxo que define como um client obtém um token. Fluxos diferentes existem porque situações diferentes exigem abordagens diferentes.

### Client Credentials

Para comunicação máquina a máquina, sem usuário humano envolvido. O client se autentica com `clientId` e `clientSecret` e recebe um token. O token representa a própria aplicação, não uma pessoa.

```
[Client]  ──  POST /oauth2/token
              Authorization: Basic base64(clientId:clientSecret)
              Body: grant_type=client_credentials&scope=GERENTE

          ◄─  { "access_token": "eyJ..." }

[Client]  ──  GET /autores
              Authorization: Bearer eyJ...

          ◄─  [resposta da API]
```

### Authorization Code

Para fluxos com usuário humano. Tem duas etapas obrigatórias.

**Etapa 1 — Obter o código**

O usuário acessa `/oauth2/authorize` pelo browser. Se não estiver logado, é redirecionado para `/login`. Após o login, o Authorization Server redireciona o browser para o `redirectUri` cadastrado, com um `code` temporário na URL:

```
GET /oauth2/authorize
    ?client_id=meu-client
    &response_type=code
    &redirect_uri=http://localhost:8080/authorized
    &scope=GERENTE

→ Redireciona para /login (se necessário)
→ Usuário faz login
→ Redireciona para: http://localhost:8080/authorized?code=ABC123XYZ
```

O `code` é de uso único e expira em segundos.

**Etapa 2 — Trocar o código pelo token**

```
POST /oauth2/token
Authorization: Basic base64(meu-client:client-secret)
Body: grant_type=authorization_code
      code=ABC123XYZ
      redirect_uri=http://localhost:8080/authorized

◄─ { "access_token": "eyJ...", "refresh_token": "..." }
```

### Refresh Token

Quando o `access_token` expira, o client usa o `refresh_token` para pedir um novo sem que o usuário precise fazer login de novo:

```
POST /oauth2/token
Authorization: Basic base64(clientId:clientSecret)
Body: grant_type=refresh_token
      refresh_token=<refresh token recebido anteriormente>

◄─ { "access_token": "eyJ..." (novo) }
```

No projeto, o `access_token` expira em 60 minutos e o `refresh_token` em 90 minutos. O client tem uma janela de 30 minutos para renovar o acesso sem novo login.

---

## 10. O caminho de uma requisição autenticada

Após o token ser emitido, este é o caminho que toda requisição autenticada percorre:

```
1. Client envia: GET /autores
                 Authorization: Bearer eyJ...

2. SecurityFilterChain (@Order padrão) intercepta a requisição

3. JwtDecoder extrai e valida o JWT:
   - Busca a chave pública em /oauth2/jwks (cacheia após a primeira vez)
   - Verifica a assinatura com a chave pública
   - Verifica se o token não expirou

4. Se válido: popula o SecurityContext com as permissões do token

5. Spring verifica se o usuário tem as permissões necessárias para o endpoint

6. Endpoint executa e retorna a resposta
```
