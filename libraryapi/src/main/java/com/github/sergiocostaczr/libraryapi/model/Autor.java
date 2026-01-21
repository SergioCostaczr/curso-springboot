package com.github.sergiocostaczr.libraryapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.EmbeddedColumnNaming;
import org.springframework.context.annotation.DependsOn;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "autor", schema = "public")
@Getter
@Setter
@ToString(exclude = "livros")
public class Autor {

    @Id //PK
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", length = 100, nullable = false)
    private String name;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "nacionalidade",length = 50, nullable = false)
    private String nacionalidade;

    @OneToMany(mappedBy = "autor",fetch = FetchType.LAZY) //Entidade não possui a coluna, apenas o mapaeamento OneToMany.
    private List<Livro> livros;

    public Autor() {
    }
}
