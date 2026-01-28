package com.github.sergiocostaczr.libraryapi.validator;

import com.github.sergiocostaczr.libraryapi.exceptions.RegistroDuplicadoException;
import com.github.sergiocostaczr.libraryapi.model.Autor;
import com.github.sergiocostaczr.libraryapi.repository.AutorRepository;
import com.github.sergiocostaczr.libraryapi.service.AutorService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AutorValidator {

    private AutorRepository autorRepository;

    public AutorValidator(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }


    public void validar(Autor autor){
        if(existeAutorCadastrado(autor)){
            throw new RegistroDuplicadoException("Autor ja cadastrado!");

        }


    }

    private boolean existeAutorCadastrado(Autor autor){
        Optional<Autor> autorOptional = autorRepository.findByNomeAndDataNascimentoAndNacionalidade(autor.getNome(), autor.getDataNascimento(),autor.getNacionalidade());


        if (autor.getId() == null){
            return  autorOptional.isPresent();
        }

        return !autor.getId().equals(autorOptional.get().getId()) && autorOptional.isPresent();

    }






}
