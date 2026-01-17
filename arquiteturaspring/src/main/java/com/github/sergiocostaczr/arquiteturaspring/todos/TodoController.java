package com.github.sergiocostaczr.arquiteturaspring.todos;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("todos")
public class TodoController {

    private TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @PostMapping
    public TodoEntiy salvar(@RequestBody TodoEntiy todo){
        try {
            return service.salvar(todo);
        } catch (IllegalArgumentException e) {
            var mensagemErro = e.getMessage();
            throw new ResponseStatusException(HttpStatus.CONFLICT, mensagemErro);
        }
    }

    @PutMapping("{id}")
    public void atualizarStatus(@PathVariable ("id") Integer id, @RequestBody TodoEntiy todo){
        todo.setId(id);
        service.atualizarStatus(todo);
    }

    @GetMapping("{id}")
    public TodoEntiy buscar(@PathVariable("id") Integer id){
        return service.buscar(id);
    }
}
