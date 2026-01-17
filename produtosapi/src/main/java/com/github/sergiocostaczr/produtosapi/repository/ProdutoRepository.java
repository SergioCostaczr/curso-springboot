package com.github.sergiocostaczr.produtosapi.repository;

import com.github.sergiocostaczr.produtosapi.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto,String> {
    List<Produto> findByNome(String nome);//Apos findBy o "nome" precisa ser igual na propiedade da entidade,
    // porem comeca com caixa alta como no get
}
