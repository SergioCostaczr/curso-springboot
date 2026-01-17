package com.github.sergiocostaczr.produtosapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

//POJO
@Entity
@Table (name = "produto")//Opcional, pois é o mesmo ambos tanto no banco quanto aqui
public class Produto {

    @Id
    @Column(name = "id")//Opcional, pois é o mesmo ambos tanto no banco quanto aqui
    private String id;

    @Column(name = "nome")//Opcional, pois é o mesmo ambos tanto no banco quanto aqui
    private String nome;

    @Column(name = "descricao")//Opcional, pois é o mesmo ambos tanto no banco quanto aqui
    private String descricao;

    @Column//Opcional, pois é o mesmo ambos tanto no banco quanto aqui
    private Double preco;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", preco=" + preco +
                '}';
    }
}
