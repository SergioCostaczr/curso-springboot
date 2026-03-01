package com.github.sergiocostaczr.libraryapi.controller;

import com.github.sergiocostaczr.libraryapi.security.CustomAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller //Espera paginas html
public class LoginViewController {

    @GetMapping("/login")
    public String paginaLogin(){
        //ViewResolver
        return "login";
    }
    @GetMapping("/teste")
    public String paginaTeste(){
        //ViewResolver
        return "teste";
    }

    @GetMapping("/")
    @ResponseBody           //Injetnado auth
    public String paginaHome(Authentication authentication){
        if (authentication instanceof CustomAuthentication customAuthentication){
            System.out.println(customAuthentication.getUsuario());
        }

       return "Ola " + authentication.getName();
    }
}
