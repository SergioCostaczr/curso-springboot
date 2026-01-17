package com.github.sergiocostaczr.arquiteturaspring.todos;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_todo")
public class TodoEntiy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private Integer id;
    private String descricao;
    @Column(name = "fl_conlcuido")
    private Boolean concluido;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getConcluido() {
        return concluido;
    }

    public void setConcluido(Boolean concluido) {
        this.concluido = concluido;
    }
}
