package com.github.sergiocostaczr.produtosapi.controller;

import com.github.sergiocostaczr.produtosapi.model.Produto;
import com.github.sergiocostaczr.produtosapi.repository.ProdutoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("produtos")
public class ProdutoController {


    private ProdutoRepository produtoRepository;

    public ProdutoController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @PostMapping
    public Produto salvar(@RequestBody Produto produto){ //Diz para o spring colocar os dados do json no objeto
        System.out.println("Produto recebido: " + produto);
        var id = UUID.randomUUID().toString();
        produto.setId(id);
        produtoRepository.save(produto);
        return produto; //Transforma por padrao em java
    }

    @GetMapping("/{id}")//Nome do parametro entre chaves
    public Produto obterPorId(@PathVariable("id") String id){//Dentro de path colocamos o parametro da url
        return produtoRepository.findById(id).orElse(null);
    }

    @DeleteMapping("/{id}")//Recebe id via url
    public void deletar(@PathVariable("id") String id){
        produtoRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public void atualizar(@PathVariable("id") String id, @RequestBody Produto produto){
       produto.setId(id);
       produtoRepository.save(produto);//Save tanto para salvar quanto para atualizar, se vier com id vai atualizar
    }

    @GetMapping
    public List<Produto> buscar(@RequestParam("nome") String nome){
        return produtoRepository.findByNome(nome);

    }

}
