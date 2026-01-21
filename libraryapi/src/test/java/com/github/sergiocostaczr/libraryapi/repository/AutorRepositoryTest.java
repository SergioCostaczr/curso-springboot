package com.github.sergiocostaczr.libraryapi.repository;

import com.github.sergiocostaczr.libraryapi.model.Autor;
import com.github.sergiocostaczr.libraryapi.model.GeneroLivro;
import com.github.sergiocostaczr.libraryapi.model.Livro;
import net.minidev.json.JSONUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest //Sobe contexto do springboot
public class AutorRepositoryTest {

    @Autowired
    AutorRepository repository;

    @Autowired
    LivroRepository livroRepository;

    @Test
    public void salvarTest() {
        Autor autor = new Autor();
        autor.setName("mario");
        autor.setNacionalidade("Brasileiro");
        autor.setDataNascimento(LocalDate.of(1950, 1, 21));

        Autor save = repository.save(autor);
        System.out.println("Autor salvo " + save);

    }

    @Test
    public void atualizarTest() {
        UUID uuid = UUID.fromString("373fdf03-e95f-47a0-9c47-c961cf2702ef");
        Optional<Autor> possivelAutor = repository.findById(uuid);

        if (possivelAutor.isPresent()) {
            Autor autorEncontrado = possivelAutor.get();

            System.out.println("Dados do autor: ");
            System.out.println(possivelAutor.get());

            autorEncontrado.setDataNascimento(LocalDate.of(1990,10,22));

            repository.save(autorEncontrado);
        }
    }

    @Test
    public void listarTest(){
        List<Autor> listaAutor = repository.findAll();

        listaAutor.forEach(System.out::println);
    }

    @Test
    public void countTest(){
        System.out.println("contagem de autores: " + repository.count());
    }

    @Test
    public void deleteByIdTest(){
        UUID uuid = UUID.fromString("72b4631d-321d-4062-8799-a23feaf3b7cb");
        repository.deleteById(uuid);
    }

    @Test
    public void deleteTest(){
        UUID uuid = UUID.fromString("373fdf03-e95f-47a0-9c47-c961cf2702ef");

        Optional<Autor> byId = repository.findById(uuid);

        if (byId.isPresent()){
            repository.deleteById(uuid);
        }
    }

    @Test
    @Transactional
    public void salvarAutorComLivrosTest(){
        Autor autor = new Autor();
        autor.setName("pedro");
        autor.setNacionalidade("brasileiro");
        autor.setDataNascimento(LocalDate.of(1900, 12,10));

        Livro livro = new Livro();
        livro.setIsbn("91293-2132");
        livro.setPreco(BigDecimal.valueOf(100));
        livro.setGenero(GeneroLivro.BIOGRAFIA);
        livro.setTitulo("Arvors");
        livro.setDataPublicacao(LocalDate.of(1940,1,2));
        livro.setAutor(autor);

        Livro livro1 = new Livro();
        livro1.setIsbn("91293-2132");
        livro1.setPreco(BigDecimal.valueOf(100));
        livro1.setGenero(GeneroLivro.CIENCIA);
        livro1.setTitulo("Acvxzc");
        livro1.setDataPublicacao(LocalDate.of(1910,1,2));
        livro1.setAutor(autor);

        autor.setLivros(new ArrayList<>());
        autor.getLivros().add(livro);
        autor.getLivros().add(livro1);

        repository.save(autor);
        livroRepository.saveAll(autor.getLivros());
 }

    @Test

    public void listarLivrosAutor(){
        UUID id = UUID.fromString("373fdf03-e95f-47a0-9c47-c961cf2702ef");
        Autor autor = repository.findById(id).orElse(null);

        //Em autor temos o mapeamento dos livros como lazy
        List<Livro> lista = livroRepository.findByAutor(autor);
        autor.getLivros().forEach(System.out::println);

    }


}