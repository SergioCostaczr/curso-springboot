package com.github.sergiocostaczr.libraryapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.EmbeddedColumnNaming;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "autor", schema = "public")
@Getter
@Setter
@ToString(exclude = "livros")
@EntityListeners(AuditingEntityListener.class)
public class Autor {

    @Id //PK
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "nacionalidade",length = 50, nullable = false)
    private String nacionalidade;

    @OneToMany(mappedBy = "autor",fetch = FetchType.LAZY) //Entidade não possui a coluna, apenas o mapaeamento OneToMany.
    private List<Livro> livros;

    // Toda vez q for persistir coloca a data atual.
    @CreatedDate
    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    // Toda vez q atualiza.
    @LastModifiedDate
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    private UUID idUsuario;

    public Autor() {
    }
}
