package com.github.sergiocostaczr.libraryapi.controller.mappers;


import com.github.sergiocostaczr.libraryapi.controller.dto.CadastroLivroDTO;
import com.github.sergiocostaczr.libraryapi.controller.dto.ResultadoPesquisaLivroDTO;
import com.github.sergiocostaczr.libraryapi.model.Livro;
import com.github.sergiocostaczr.libraryapi.repository.AutorRepository;
import com.github.sergiocostaczr.libraryapi.repository.LivroRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

                                            //Usar outros mappers
@Mapper(componentModel = "spring", uses = AutorMapper.class )
public abstract class LivroMapper {

    @Autowired
    AutorRepository autorRepository;

    @Mapping(target = "autor", expression = "java( autorRepository.findById(dto.idAutor()).orElse(null) ) ")
     public abstract Livro toEntity(CadastroLivroDTO dto);


    public abstract ResultadoPesquisaLivroDTO toDTO(Livro livro);
}
