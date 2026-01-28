package com.github.sergiocostaczr.libraryapi.repository;

import com.github.sergiocostaczr.libraryapi.model.Autor;
import com.github.sergiocostaczr.libraryapi.model.GeneroLivro;
import com.github.sergiocostaczr.libraryapi.model.Livro;
import org.hibernate.annotations.ListIndexBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * @see LivroRepositoryTest
 *
 */
public interface LivroRepository extends JpaRepository<Livro, UUID> {

    //https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html


    //Query Method
    // select * from livro where id_autor = id
    List<Livro> findByAutor(Autor autor);
    //getAutor

    List<Livro> findByTitulo(String titulo);
    //get'T'itulo

    // select * from livro where isbn = ?
    List<Livro> findByIsbn(String isbn);

    // select * from livro where titulo = ? and isbn = ?
    List<Livro> findByTituloAndPreco(String isbn, BigDecimal bigDecimal);

    // select * from livro where titulo = ? or isbn = ?
    List<Livro> findByTituloOrIsbn(String isbn, String s);

    // select * from livro weher data_publicacao between ? and ?
    List<Livro> findByDataPublicacaoBetween(LocalDate inicio, LocalDate fim);

    // JPQL -> referencia as entidades e as propiedades
    @Query(" select l from Livro as l order by l.titulo, l.preco") //l.'titulo' propiedade que declaramos na entidade
    List<Livro> listaTodosOrdenadoPorTiuloEPreco();

    /**
     *  select a.*
     *  from livro l
     *  join autor a on a.id = l.id_autor
     * @return
     */
    @Query(" select a from Livro l join l.autor a")
    List<Autor> listarAutoresDosLivros();

    // select distinct l.* from Livro l
    @Query("select distinct l.titulo from Livro l")
    List<String> listarNomesDiferentesLivros();


    @Query("""
            select l.genero
            from Livro l
            join l.autor a
            where a.nacionalidade = 'Brasileira'
            order by l.genero
            """)
    List<String> listarGenerosAutoresBrasileiros();


    // named parameters -> parametros nomeados
    @Query("select l from Livro l where l.genero = :genero order by :paramOrdenacao")
    List<Livro> findByGenero(@Param("genero") GeneroLivro generoLivro,
                             @Param("paramOrdenacao") String nomeDaPropiedade);

    // positional parameters
    @Query("select l from Livro l where l.genero = ?1 order by ?2")
    List<Livro> findByGeneroPositionalParameters(GeneroLivro generoLivro,

                                                 String nomeDaPropiedade);
    @Transactional
    @Modifying
    @Query( "delete from Livro where genero = ?1")
    void deleteByGenero(GeneroLivro generoLivro);

    @Transactional
    @Modifying
    @Query( "update Livro set dataPublicacao = ?1")
    void updateDataPublicacao(LocalDate novaData);


    boolean existsByAutor(Autor autor);





















}
