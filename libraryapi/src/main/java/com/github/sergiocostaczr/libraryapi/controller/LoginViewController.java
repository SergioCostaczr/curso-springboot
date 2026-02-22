package com.github.sergiocostaczr.libraryapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
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
}
