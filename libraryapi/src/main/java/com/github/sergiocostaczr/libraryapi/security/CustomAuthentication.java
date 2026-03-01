package com.github.sergiocostaczr.libraryapi.security;

import com.github.sergiocostaczr.libraryapi.model.Usuario;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

// Toda a aplicação independente do tipo autenticação, seja basic, form, google no final produzimos uma instância dessa classe
@RequiredArgsConstructor
@Getter
public class CustomAuthentication implements Authentication {

    private final Usuario usuario;

    @Override //collection de qualquer tipo q implemente GrantedAuthority. Ctrl + Alt para ver as implementações de GrantedAuthority
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this
                .usuario
                .getRoles()                                         //"ROLE_"+
                .stream().map(role -> new SimpleGrantedAuthority(role))
                .toList();
    }

    @Override
    public @Nullable Object getCredentials() {
        return null;
    }

    @Override
    public @Nullable Object getDetails() {
        return usuario;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return usuario;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    @Override
    public String getName() {
        return usuario.getLogin();
    }
}
