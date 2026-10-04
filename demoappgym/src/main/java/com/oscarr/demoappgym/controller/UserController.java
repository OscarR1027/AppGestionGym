package com.oscarr.demoappgym.controller;

import java.util.List;
import java.util.Optional;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oscarr.demoappgym.model.entity.User;
import com.oscarr.demoappgym.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping ("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService=userService;
    }
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User newUser=userService.save(user);
            return ResponseEntity.ok(newUser);
    }
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUser(){
        List<User> users= userService.findAll();
            return ResponseEntity.ok(users);
    } 
    @GetMapping ("/{id}/")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        Optional<User> user= userService.findById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.notFound().build();
            }
    }
    //TODO:Mejorar las excepciones en las busquedas
    
    
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
            return ResponseEntity.noContent().build();
    }
}
