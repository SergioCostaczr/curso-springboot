package com.github.sergiocostaczr.libraryapi.repository.specs;

import com.github.sergiocostaczr.libraryapi.model.GeneroLivro;
import com.github.sergiocostaczr.libraryapi.model.Livro;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import tools.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

/**
 * @see <a href="https://jakarta.ee/learn/docs/jakartaee-tutorial/current/persist/persistence-criteria/persistence-criteria.html">Criteria API</a>
 */
public class LivroSpecs {

    public static Specification<Livro> isbnEqual(String isbn){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isbn"),isbn);
    }

    public static Specification<Livro> tituloLike(String titulo){
        //upper(livro.titulo) like (%:param%)
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.upper(root.get("titulo")),"%" + titulo.toUpperCase() +"%");
    }

    public static Specification<Livro> generoEqual(GeneroLivro generoLivro){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("genero"), generoLivro);
    }

    public static Specification<Livro> anoPublicacaoEqual(Integer anoPublicacao){
        // and to_char(data_publicacao, 'YYYY') = :anoPublicacao
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.function
                        ("to_char", String .class, root.get("dataPublicacao"),criteriaBuilder.literal("YYYY")) ,anoPublicacao.toString());
    }

    /*
    *       select *
     *      from livro as l join autor as a on a.id = l.id_autor
     *      where upper(a.nome) like upper('%fran%')
     */


    public static Specification<Livro> nomeAutorLike(String nome){
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> joinAutor = root.join("nome", JoinType.LEFT);

            return criteriaBuilder.like(criteriaBuilder.upper(joinAutor.get("nome")),"%" + nome.toUpperCase() + "%");

//          return criteriaBuilder.like(criteriaBuilder.upper(root.get("autor").get("nome")),"%" + nome.toUpperCase() + "%");
        };
    }
}
