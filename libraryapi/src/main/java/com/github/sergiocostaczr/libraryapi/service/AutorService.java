package com.github.sergiocostaczr.libraryapi.service;

import com.github.sergiocostaczr.libraryapi.exceptions.OperacaoNaoPermitidaException;
import com.github.sergiocostaczr.libraryapi.model.Autor;
import com.github.sergiocostaczr.libraryapi.repository.AutorRepository;
import com.github.sergiocostaczr.libraryapi.repository.LivroRepository;
import com.github.sergiocostaczr.libraryapi.validator.AutorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor // Gera construtor com as variaveis final.
public class AutorService {

    private final AutorRepository autorRepository;
    private final LivroRepository livroRepository;
    private final AutorValidator validator;


    public Autor salvar (Autor autor){
        validator.validar(autor);
        return autorRepository.save(autor);
    }

    public void atualizar (Autor autor){
        if (autor.getId() == null){
            throw new IllegalArgumentException("Para atualizar é necessario que autor ja esteja salvo na base");
        }
        validator.validar(autor);
        autorRepository.save(autor);
    }



    public Optional<Autor> obterPorId(UUID uuid){
        return autorRepository.findById(uuid);
    }


    public void deletar(Autor autor) {
        if(possuiLivro(autor)){
            throw new OperacaoNaoPermitidaException("Não é permitido autor que possui livros cadastrados");
        }
        autorRepository.delete(autor);
    }

    public List<Autor> pesquisa(String nome, String nacionalidade){

        if (nome != null && nacionalidade != null){
            return  autorRepository.findByNomeAndNacionalidade(nome,nacionalidade);
        }

        if (nome!= null){
            return autorRepository.findByNome(nome);
        }

        if (nacionalidade != null){
        return autorRepository.findByNacionalidade(nacionalidade);
        }

        return autorRepository.findAll();
    }

    public boolean possuiLivro(Autor autor){
        return livroRepository.existsByAutor(autor);

    }
}
