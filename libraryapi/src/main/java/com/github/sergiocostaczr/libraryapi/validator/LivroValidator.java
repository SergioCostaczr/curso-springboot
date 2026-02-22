package com.github.sergiocostaczr.libraryapi.validator;

import com.github.sergiocostaczr.libraryapi.exceptions.CampoInvalidoException;
import com.github.sergiocostaczr.libraryapi.exceptions.RegistroDuplicadoException;
import com.github.sergiocostaczr.libraryapi.model.Livro;
import com.github.sergiocostaczr.libraryapi.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LivroValidator {

    private static final int ANO_EXIGENCIA_PRECO = 2020;

    private final LivroRepository livroRepository;

    public void validar(Livro livro){
        if (existeLivroIsbn(livro)){
            throw new RegistroDuplicadoException("ISBN ja cadastrado");
        }

        if (isPrecoObrigatorioNulo(livro)){
            throw new CampoInvalidoException("preco", "Para livros com ano a partir de 2020 o preco é obrigatorio");
        }
    }

    private boolean isPrecoObrigatorioNulo(Livro livro) {
        return livro.getPreco() == null &&
                livro.getDataAtualizacao().getYear() >= ANO_EXIGENCIA_PRECO;
    }

    private boolean existeLivroIsbn(Livro livro){
        Optional<Livro> livroEncontrado = livroRepository.findByIsbn(livro.getIsbn());

        // caso seja registro
        if(livro.getId() == null){
            return livroEncontrado.isPresent();
        }

        // atualizacao
        return livroEncontrado
                .map(Livro::getId)
                .stream()
                .anyMatch(id -> !id.equals(livro.getId())); /*compara o id do optional com o do parametro,
                                                                    se forem diferentes retorna true para cair na excecao
*/
    }
}
