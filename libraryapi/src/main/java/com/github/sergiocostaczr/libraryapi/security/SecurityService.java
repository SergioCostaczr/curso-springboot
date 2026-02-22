package com.github.sergiocostaczr.libraryapi.security;

import com.github.sergiocostaczr.libraryapi.model.Usuario;
import com.github.sergiocostaczr.libraryapi.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityService {

    private final UsuarioService usuarioService;

    public Usuario obterUsuarioLogado(){
        //Busca Authentication no contexto do spring
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String login = userDetails.getUsername();

        return usuarioService.obterPorLogin(login);
    }
}
