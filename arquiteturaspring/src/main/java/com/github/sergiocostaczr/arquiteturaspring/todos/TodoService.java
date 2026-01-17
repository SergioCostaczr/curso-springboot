package com.github.sergiocostaczr.arquiteturaspring.todos;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

@Component //@Service
public class TodoService {

    private TodoRepository repository;
    private TodoValidator validator;
    private MailSender mailSender;

    public TodoService(TodoRepository repository, TodoValidator validator, MailSender mailSender) {
        this.repository = repository;
        this.validator = validator;
        this.mailSender = mailSender;
    }

    public TodoEntiy salvar (TodoEntiy novoTodoEntiy){
        validator.validar(novoTodoEntiy);
        return repository.save(novoTodoEntiy);
    }

    public void atualizarStatus(TodoEntiy todo){
        repository.save(todo);
        String status = todo.getConcluido() == Boolean.TRUE ? "concluido" : "nao concluido";
        mailSender.enviar("Todo " + todo.getDescricao() +" foi atualizado para "+ status);
    }

    public TodoEntiy buscar(Integer id){
        return repository.findById(id).orElse(null);
    }

}
