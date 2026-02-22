package com.github.sergiocostaczr.libraryapi.service;

import com.github.sergiocostaczr.libraryapi.model.GeneroLivro;
import com.github.sergiocostaczr.libraryapi.model.Livro;
import com.github.sergiocostaczr.libraryapi.model.Usuario;
import com.github.sergiocostaczr.libraryapi.repository.LivroRepository;
import com.github.sergiocostaczr.libraryapi.repository.specs.LivroSpecs;
import com.github.sergiocostaczr.libraryapi.security.SecurityService;
import com.github.sergiocostaczr.libraryapi.validator.LivroValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository livroRepository;
    private final LivroValidator livroValidator;
    private final SecurityService securityService;


    public Livro salvar(Livro livro) {
        livroValidator.validar(livro);
        Usuario usuario = securityService.obterUsuarioLogado();
        livro.setUsuario(usuario);
        return livroRepository.save(livro);
    }

    public Optional<Livro> obterPorId(UUID id){
        return livroRepository.findById(id);
    }

    public void deletar(Livro livro){
        livroRepository.delete(livro);
    }


    public Page<Livro> pesquisa(String isbn,
                                String titulo,
                                String nomeAutor,
                                GeneroLivro generoLivro,
                                Integer anoPublicacao,
                                Integer pagina,
                                Integer tamanhoPagina){

        // select * from livro where isbn = :isbn and nomeAutor =


//        Specification<Livro> spcs = Specification.
//                where(LivroSpecs.isbnEqual(isbn))
//                .and(LivroSpecs.tituloLike(titulo)
//                .and(LivroSpecs.generoEqual(generoLivro));

        // select * from livro where 0 = 0
        // inicializacao da spec
        Specification<Livro> specs = Specification.
                where((root, query, criteriaBuilder) -> criteriaBuilder.conjunction());

        if (isbn != null){
            specs = specs.and(LivroSpecs.isbnEqual(isbn));
        }

        if (titulo != null){
            specs = specs.and(LivroSpecs.tituloLike(titulo));
        }

        if (generoLivro != null){
            specs = specs.and(LivroSpecs.generoEqual(generoLivro));
        }

        if (anoPublicacao !=null){
            specs = specs.and(LivroSpecs.anoPublicacaoEqual(anoPublicacao));
        }

                                    // implementacao do pageable
        Pageable pageRequest = PageRequest.of(pagina,tamanhoPagina);

        return livroRepository.findAll(specs,pageRequest);

    }

    public void atualizar(Livro livro) {
        if (livro.getId() == null){
            throw new IllegalArgumentException("Para atualizar é necessario que livro ja esteja salvo na base");
        }
        livroValidator.validar(livro);
        livroRepository.save(livro);

    }
}
