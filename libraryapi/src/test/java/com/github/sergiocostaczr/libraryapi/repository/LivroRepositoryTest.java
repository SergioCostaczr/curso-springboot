package com.github.sergiocostaczr.libraryapi.repository;

import com.github.sergiocostaczr.libraryapi.model.Autor;
import com.github.sergiocostaczr.libraryapi.model.GeneroLivro;
import com.github.sergiocostaczr.libraryapi.model.Livro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LivroRepositoryTest {

    @Autowired
    LivroRepository repository;

    @Autowired
    AutorRepository autorRepository;


    @Test
    public void salvarTest(){
        Livro livro = new Livro();
        livro.setIsbn("91293-2132");
        livro.setPreco(BigDecimal.valueOf(100));
        livro.setGenero(GeneroLivro.FICCAO);
        livro.setTitulo("UFO");
        livro.setDataPublicacao(LocalDate.of(1980,1,2));

        Autor autor = autorRepository
                .findById(UUID.fromString("373fdf03-e95f-47a0-9c47-c961cf2702ef"))
                .orElse(null);

        livro.setAutor(autor);

        //livro.setAutor(new Autor());

        repository.save(livro);

    }

    @Test
    public void salvarAutorELivroTest(){
        Livro livro = new Livro();
        livro.setIsbn("91293-2132");
        livro.setPreco(BigDecimal.valueOf(100));
        livro.setGenero(GeneroLivro.FICCAO);
        livro.setTitulo("outro livro");
        livro.setDataPublicacao(LocalDate.of(1980,1,2));

        Autor autor = new Autor();
        autor.setNome("joao");
        autor.setNacionalidade("Brasileiro");
        autor.setDataNascimento(LocalDate.of(1950, 1, 21));

        autorRepository.save(autor);

        livro.setAutor(autor);

        repository.save(livro);

    }


    @Test
    public void atualizarAutorLivro(){
        UUID id = UUID.fromString("ce24aced-2b18-4091-9bef-2516dfa3f396");
        Livro livroParaAtualizar = repository.findById(id).orElse(null);

        UUID idAutor = UUID.fromString("72b4631d-321d-4062-8799-a23feaf3b7cb");

        Autor autor = autorRepository.findById(idAutor).orElse(null);

        livroParaAtualizar.setAutor(autor);

        repository.save(livroParaAtualizar);

        }
    @Test
    public void deletar(){
        UUID id = UUID.fromString("ce24aced-2b18-4091-9bef-2516dfa3f396");

        repository.deleteById(id);

    }

    @Test
    @Transactional //Ao abrir essa transação conseguimos acessar autor que esta com fetchtype lazy
    public void buscarLivroTest(){
        UUID id = UUID.fromString("ce24aced-2b18-4091-9bef-2516dfa3f396");
        Livro livro = repository.findById(id).orElse(null);
        System.out.println("Livro: ");
        System.out.println(livro.getTitulo());
        System.out.println("autor:");
        System.out.println(livro.getAutor().getNome());
    }

    @Test
    void pesquisaPorTituloTest(){
        List<Livro> list = repository.findByTitulo("UFO");
        list.forEach(System.out::println);
    }

    @Test
    void pesquisaPorIsbnTest(){
        List<Livro> list = repository.findByIsbn("91293-2132");
        list.forEach(System.out::println);
    }

    @Test
    void pesquisaPorTituloAndPrecoTest(){
        BigDecimal preco = BigDecimal.valueOf(200);
        String  titulo = "UFO";

        List<Livro> list = repository.findByTituloAndPreco(titulo,preco);
        list.forEach(System.out::println);
    }

    @Test
    void listarLivrosComQueryJPQL(){
        List<String> resultado = repository.listarNomesDiferentesLivros();
        resultado.forEach(System.out::println);
    }

    @Test
    void listarGenerosAutoresBrasileiros(){
        List<String> resultado = repository.listarGenerosAutoresBrasileiros();
        resultado.forEach(System.out::println);
    }
    @Test
    void listarPorGeneroQueryParam(){
        List<Livro> resultado = repository.findByGenero(GeneroLivro.FICCAO, "dataPublicacao");
        resultado.forEach(System.out::println);
    }

    @Test
    void deleteByGenero(){
        repository.deleteByGenero(GeneroLivro.CIENCIA);
    }
    @Test
    void updateDataPublicacao(){
        repository.updateDataPublicacao(LocalDate.of(2000,10,01));
    }


//   @Test
//   public void salvarCascadeTest(){
//       Livro livro = new Livro();
//       livro.setIsbn("91293-2132");
//       livro.setPreco(BigDecimal.valueOf(100));
//       livro.setGenero(GeneroLivro.FICCAO);
//       livro.setTitulo("outro livro");
//       livro.setDataPublicacao(LocalDate.of(1980,1,2));

//       Autor autor = new Autor();
//       autor.setName("joao");
//       autor.setNascionalidade("Brasileiro");
//       autor.setDataNascimento(LocalDate.of(1950, 1, 21));

//       livro.setAutor(autor);

//       repository.save(livro);

//   }













}