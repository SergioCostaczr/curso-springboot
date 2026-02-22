package com.github.sergiocostaczr.libraryapi.controller.mappers;

import com.github.sergiocostaczr.libraryapi.controller.dto.UsuarioDTO;
import com.github.sergiocostaczr.libraryapi.model.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioDTO toDTO(Usuario usuario);

    Usuario toEntity(UsuarioDTO dto);
}
