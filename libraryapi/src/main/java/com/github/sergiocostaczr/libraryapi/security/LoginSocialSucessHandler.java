package com.github.sergiocostaczr.libraryapi.security;

import com.github.sergiocostaczr.libraryapi.model.Usuario;
import com.github.sergiocostaczr.libraryapi.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor                        //implementacao de AuthenticationSuccessHandler
public class LoginSocialSucessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
//  O AuthenticationSuccessHandler é chamado logo após uma autenticação bem sucedida.
//  Quem faz a validação do token de autenticação é o própio Google, por isso o token já vem autenticado.
//  Quando o Google termina de validar ele o Spring ja cria um OAuth2AuthenticationToken

    private static final String SENHA_PADRAO = "123";

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        OAuth2AuthenticationToken auth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;

//        Pega o usuário vindo do Google
        OAuth2User oAuth2User = auth2AuthenticationToken.getPrincipal();

//        Extrai o email
        String email = oAuth2User.getAttribute("email");

//        Busca o usuário do banco da aplicação
        Usuario user = usuarioService.obterPorEmail(email);

        if (user == null){
            user = cadastrarUsuarioNaBase(email);
        }

//        Cria o CustomAuthentication
//        Aqui ele substitui completamente o token do Google.
        authentication = new CustomAuthentication(user);

//        Atualiza o SecurityContext manualmente
        SecurityContextHolder.getContext().setAuthentication(authentication);

//        Continua o fluxo normal
        super.onAuthenticationSuccess(request, response, authentication);

    }

    private Usuario cadastrarUsuarioNaBase(String email) {
        Usuario user;
        user = new Usuario();
        user.setEmail(email);

        user.setLogin(obterLoginApartirEmail(email));

        user.setSenha(SENHA_PADRAO);
        user.setRoles(List.of("OPERADOR"));
        usuarioService.salvar(user);
        return user;
    }
    private String obterLoginApartirEmail(String email){
        return email.substring(0,email.indexOf("@"));
    }
}
