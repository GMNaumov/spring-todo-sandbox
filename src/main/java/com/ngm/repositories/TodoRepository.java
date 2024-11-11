package com.ngm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ngm.models.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    
}
