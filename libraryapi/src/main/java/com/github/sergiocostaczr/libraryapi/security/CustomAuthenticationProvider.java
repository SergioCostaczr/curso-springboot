package com.github.sergiocostaczr.libraryapi.security;

import com.github.sergiocostaczr.libraryapi.model.Usuario;
import com.github.sergiocostaczr.libraryapi.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component// component que verifica se login e senha estão corretos. é chamado quando alguém tenta autenticar.
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        //dados do token
        String login = authentication.getName();
        String senhaDigitada = authentication.getCredentials().toString();

        // localizar usuario
        Usuario usuario = usuarioService.obterPorLogin(login);

        if (usuario == null){
            throw getErrorUsuarioNaoEncontrado();
        }

        String senhaCriptografada = usuario.getSenha();

        // verificar
        boolean senhasBatem = passwordEncoder.matches(senhaDigitada, senhaCriptografada);

        if (senhasBatem){
            return new CustomAuthentication(usuario);
        }
        throw getErrorUsuarioNaoEncontrado();
    }

    private static UsernameNotFoundException getErrorUsuarioNaoEncontrado() {
        return new UsernameNotFoundException("Usuario e/ou senha incorretos!");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        //tipos de authentication que suportam esse metodo
        //basicamente diz "Eu só sei autenticar UsernamePasswordAuthenticationToken"
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
