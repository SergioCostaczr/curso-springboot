package com.github.sergiocostaczr.libraryapi.controller.mappers;

import com.github.sergiocostaczr.libraryapi.controller.dto.AutorDTO;
import com.github.sergiocostaczr.libraryapi.model.Autor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AutorMapper {

//    @Mapping(source = "id", target = "id") por padrao pega o mesmo nome, senao precisa definir source e target
    Autor toEntity(AutorDTO dto);

    AutorDTO toDTO(Autor autor);

}
