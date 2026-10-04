package com.oscarr.demoappgym.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oscarr.demoappgym.model.dto.UserRequest;
import com.oscarr.demoappgym.model.dto.UserResponse;
import com.oscarr.demoappgym.model.entity.User;
import com.oscarr.demoappgym.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        User user = new User();
        applyRequest(user, request);
        return ResponseEntity.ok(toResponse(userService.save(user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return userService.findById(id)
                .map(existing -> {
                    applyRequest(existing, request);
                    return ResponseEntity.ok(toResponse(userService.save(existing)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUser() {
        return ResponseEntity.ok(userService.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(toResponse(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyRequest(User user, UserRequest request) {
        user.setName(request.getName());
        user.setAddress(request.getAddress());
        user.setPassword(request.getPassword());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setMail(request.getMail());
        user.setPhysicalConditionReport(request.getPhysicalConditionReport());
        user.setRole(request.getRole());
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getCodeUser(), user.getName(), user.getAddress(),
                user.getPhoneNumber(), user.getMail(), user.getPhysicalConditionReport(),
                user.getRole() == null ? null : user.getRole().name());
    }
}
