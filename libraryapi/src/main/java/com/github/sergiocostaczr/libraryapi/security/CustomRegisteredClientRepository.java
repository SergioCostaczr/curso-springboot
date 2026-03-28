package com.github.sergiocostaczr.libraryapi.security;

import com.github.sergiocostaczr.libraryapi.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    private final ClientService clientService;
    private final TokenSettings tokenSettings;
    private final ClientSettings clientSettings;

    @Override
    public void save(RegisteredClient registeredClient) {

    }

    @Override
    public RegisteredClient findById(String id) {
        return null;
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        var client = clientService.obterPorClientId(clientId);

        if (client == null){
            return null;
        }

        //Representação de uma aplicação cliente dentro do Authorization Server.
        return RegisteredClient
                .withId(client.getId().toString())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                //redirectUri — no fluxo Authorization Code, após o usuário fazer login, o Authorization Server precisa saber para qual URL ele pode redirecionar o usuário com o código.
                // Isso existe por segurança: evita que uma aplicação maliciosa registre outro redirect URI e roube o código.
                .redirectUri(client.getRedirectURi())
                //scope — o que esse client pode solicitar. No token final, os escopos aprovados ficam gravados.
                // Assim o Resource Server consegue checar não só se o token é válido, mas se quem o pediu tinha permissão para aquele escopo específico.
                .scope(client.getScope())
                // auth metodo
                /**
                 * clientAuthenticationMethod(CLIENT_SECRET_BASIC) — define como o client prova sua identidade ao Authorization Server.
                 * CLIENT_SECRET_BASIC significa que ele manda o clientId e o clientSecret no header Authorization usando o formato Basic Auth, ou seja, base64(clientId:clientSecret)
                 */
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)

                //Grant type é o nome do fluxo que define como um client obtém um token de acesso.
                //authorizationGrantType — quais fluxos esse client tem permissão de usar. Um client pode suportar mais de um.
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                // renovar token
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                //tokenSettings — são as configurações dos tokens emitidos para esse client, definidas como bean
                .tokenSettings(tokenSettings)
//                .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofDays(1)).build())
                .clientSettings(clientSettings)
                .build();
    }
}
