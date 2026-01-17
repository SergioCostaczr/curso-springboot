package com.github.sergiocostaczr.arquiteturaspring;

import com.github.sergiocostaczr.arquiteturaspring.todos.*;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;

public class ExemploInjecaoDependencia {
    public static void main(String[] args) throws Exception {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("url");
        dataSource.setUsername("username");
        dataSource.setPassword("password");

        Connection connection = dataSource.getConnection();

        EntityManager entityManager = null;

        TodoRepository repository = null;//new SimpleJpaRepository<TodoEntiy, Integer>();
        TodoValidator validator = new TodoValidator(repository);
        MailSender mailSender = new MailSender();

        TodoService todoService = new TodoService(repository,validator,mailSender);

        //Injecao via seter, se torna opcional
        //BeanGerenciado beanGerenciado = new BeanGerenciado(null);
        //beanGerenciado.setValidator(validator);




    }
}
