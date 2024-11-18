package com.ngm.controllers;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ngm.models.Todo;
import com.ngm.repositories.TodoRepository;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
public class TodoController {
    private TodoRepository todoRepository;

    public TodoController(@Autowired TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @GetMapping("/hello")
    public String greeting() {
        return "Hello!";
    }

    @PostMapping("/todos")
    public ResponseEntity<Todo> createTodo(@Valid @RequestBody Todo todo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();

            bindingResult.getAllErrors()
            .forEach(error -> {
                String fieldName = ((FieldError)error).getField();
                String errorCode = error.getDefaultMessage();
                errors.put(fieldName, errorCode);
            });
            
            return ResponseEntity.badRequest().build();
        }

        Todo newTodo = todoRepository.save(todo);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newTodo.getId())
                .toUri();

        return ResponseEntity.created(location).body(newTodo);
    }

    @GetMapping("/todos")
    public ResponseEntity<List<Todo>> getAllTodos() {
        List<Todo> allTodos = todoRepository.findAll();
        return new ResponseEntity<>(allTodos, HttpStatus.OK);
    }

    @GetMapping("/todos/{id}")
    public ResponseEntity<Todo> getTodoById(@PathVariable("id") Long id) {
        Optional<Todo> todo = todoRepository.findById(id);

        if (todo.isPresent()) {
            return new ResponseEntity<>(todo.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/todos/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable("id") Long id, @RequestBody Todo todo) {
        return todoRepository.findById(id)
                .map(existingTodo -> {
                    existingTodo.setTitle(todo.getTitle());
                    existingTodo.setDescription(todo.getDescription());
                    existingTodo.setCompleted(todo.isCompleted());
                    return new ResponseEntity<>(todoRepository.save(existingTodo), HttpStatus.OK);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/todos/{id}/complete")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Todo> completeTodo(@PathVariable("id") Long id) {
        try {
            Todo existingTodo = todoRepository.findById(id).get();
            existingTodo.setCompleted(true);
            existingTodo = todoRepository.save(existingTodo);
            return ResponseEntity.ok(existingTodo);
        } catch (NoSuchElementException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/todos/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable("id") Long id) {
        Optional<Todo> existingTodo = todoRepository.findById(id);

        if (existingTodo.isPresent()) {
            todoRepository.delete(existingTodo.get());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
