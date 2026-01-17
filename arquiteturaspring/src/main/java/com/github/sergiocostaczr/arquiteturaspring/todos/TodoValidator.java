package com.github.sergiocostaczr.arquiteturaspring.todos;

import org.springframework.stereotype.Component;

//"pai" dos annotations stereotype
@Component
public class TodoValidator {

    private TodoRepository repository;

    public TodoValidator(TodoRepository repository) {
        this.repository = repository;
    }

    public void validar (TodoEntiy todo){
        if (existeTodoDescricao(todo.getDescricao())){
            throw new IllegalArgumentException("Ja existe um TODO com esta descricao!");
        }
    }
    private boolean existeTodoDescricao(String descricao){
        return repository.existsByDescricao(descricao);
    }

}
