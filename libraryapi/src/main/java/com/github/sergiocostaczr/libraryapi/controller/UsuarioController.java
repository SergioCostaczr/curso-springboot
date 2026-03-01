package com.github.sergiocostaczr.libraryapi.controller;

import com.github.sergiocostaczr.libraryapi.controller.dto.UsuarioDTO;
import com.github.sergiocostaczr.libraryapi.controller.mappers.UsuarioMapper;
import com.github.sergiocostaczr.libraryapi.model.Usuario;
import com.github.sergiocostaczr.libraryapi.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    private final UsuarioMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void salvar(@RequestBody @Valid UsuarioDTO dto){
        Usuario entity = mapper.toEntity(dto);
        usuarioService.salvar(entity);

    }
}
