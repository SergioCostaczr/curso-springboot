package com.github.sergiocostaczr.libraryapi.repository;

import com.github.sergiocostaczr.libraryapi.service.TransacaoService;
import org.hibernate.annotations.AnyDiscriminatorImplicitValues;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class TransacaoesTest {

    @Autowired
    TransacaoService transacaoService;


    /**
     * Abre uma transação no início de execução e no final faz um commit ou rollback.
     *
     * Commit -> confirmar as alterações
     * Rollback -> desfazer as alterações
     */
    // Toda vez que fazemos operações de escrita no db precisamos de uma transaction
    @Test
    void transacaoSimples(){
        // salvar um livro
        // salar o autor
        // alugar o livro
        // enviar email pro locatário
        // notificar que o livro saiu da livraria

        transacaoService.executar();

    }

    @Test
    void transacaoEstadoManeged(){
        transacaoService.atualizacaoSemATualizar();
    }

}
