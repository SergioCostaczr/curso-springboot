package com.github.sergiocostaczr.arquiteturaspring.todos;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<TodoEntiy, Integer> {
    boolean existsByDescricao(String descricao); //existsBy
}
