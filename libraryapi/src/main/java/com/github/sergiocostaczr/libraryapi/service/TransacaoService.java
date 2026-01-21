package com.github.sergiocostaczr.libraryapi.service;
import com.github.sergiocostaczr.libraryapi.model.Autor;
import com.github.sergiocostaczr.libraryapi.model.GeneroLivro;
import com.github.sergiocostaczr.libraryapi.model.Livro;
import com.github.sergiocostaczr.libraryapi.repository.AutorRepository;
import com.github.sergiocostaczr.libraryapi.repository.LivroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransacaoService {

    @Autowired
    private AutorRepository autorRepository;
    @Autowired
    private LivroRepository livroRepository;

    @Transactional
    public void atualizacaoSemATualizar(){
        Livro livro = livroRepository.findById(UUID.fromString("2ec85b28-4ba5-43b9-a6c2-a60b64d760d5")).orElse(null);

        livro.setDataPublicacao(LocalDate.of(2020,4,1));

        /*livroRepository.save(livro); A entidade estando no estado managed vai ser feito
         uma alteração, dps um commit para banco
         */

        /*
        Quando uma entidade está no estado managed numa transação, quando feito alguma alteração e um commit,
        as alterações vão ser enviadas para o banco de dados.
         */
    }

    @Transactional
    public void executar(){

        Autor autor = new Autor();
        autor.setName("France");
        autor.setNacionalidade("Brasileiro");
        autor.setDataNascimento(LocalDate.of(1950, 1, 21));

        autorRepository.save(autor);
        //autorRepository.saveAndFlush(autor);

        Livro livro = new Livro();
        livro.setIsbn("91293-2132");
        livro.setPreco(BigDecimal.valueOf(100));
        livro.setGenero(GeneroLivro.FICCAO);
        livro.setTitulo("Livro de france");
        livro.setDataPublicacao(LocalDate.of(1980,1,2));
        livro.setAutor(autor);

        livroRepository.save(livro);
        //livroRepository.saveAndFlush(livro);


        if (autor.getName().equals("test France")){
            throw new RuntimeException("Rollback");
        }

    }

    /// livro (titulo,..., nome_arquivo) -> id.png
    @Transactional
    public  void salvarLivro(){
        // salva o livro
        // repository.save(livro);

        // pega o id do livro = livro.getId();
        // var id = livro.getId();

        // salvar foto do livro -> bucket na nuvem
        // bucketService.salvr(livro.getFoto(), id + ".png");

        // atualizar nome do arquivo que foi salvo
        // livro.setNomeArquivoFoto(id + ".png");
    }










}
