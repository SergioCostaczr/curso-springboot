package com.github.sergiocostaczr.libraryapi.service;

import com.github.sergiocostaczr.libraryapi.model.Usuario;
import com.github.sergiocostaczr.libraryapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {


    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    public void salvar(Usuario usuario){
        String senha = usuario.getSenha();
        usuario.setSenha(passwordEncoder.encode(senha));
        usuarioRepository.save(usuario);
    }

    public Usuario obterPorLogin(String login){
        return usuarioRepository.findByLogin(login);
    }
    public Usuario obterPorEmail(String email){
        return usuarioRepository.findByEmail(email);
    }

}
