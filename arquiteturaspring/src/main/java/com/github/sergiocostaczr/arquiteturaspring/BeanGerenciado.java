package com.github.sergiocostaczr.arquiteturaspring;

import com.github.sergiocostaczr.arquiteturaspring.todos.TodoEntiy;
import com.github.sergiocostaczr.arquiteturaspring.todos.TodoValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Lazy(value = false)
@Component
//@Scope("singleton") Padrão é "singleton". Instância unica.
//@Scope("prototype") Cada requisição é uma instância, ao contrário do singleton.

//@Scope("request") Aplicação web. É instanciado na hora da requisição e existe durante a duração dela
//@Scope("session") Aplicação web. Dura enquanto a sessão. Guarda estado.
//@Scope("application") Aplicação web. Mais abrangete que session. Guarda estado.
public class BeanGerenciado {

    //private String idUsuarioLogado

//Formas de Injetar Dependência

    //Não denota obrigatoriedade e nem opcionalidade
    @Autowired
    private TodoValidator validator;

    public void utilizar(){
        TodoEntiy todoEntiy = new TodoEntiy();
        validator.validar(todoEntiy);
    }

    @Autowired
    public void setValidator(TodoValidator validator){
        this.validator = validator;
    }

    //"Para essa classe existir e funcionar, ela OBRIGATORIAMENTE precisa dessa dependência"
    public BeanGerenciado(TodoValidator validator) {
        this.validator = validator;
    }
}
