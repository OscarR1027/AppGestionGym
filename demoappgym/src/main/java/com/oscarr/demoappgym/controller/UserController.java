package com.oscarr.demoappgym.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oscarr.demoappgym.exception.BusinessException;
import com.oscarr.demoappgym.model.dto.UserRequest;
import com.oscarr.demoappgym.model.dto.UserResponse;
import com.oscarr.demoappgym.model.entity.User;
import com.oscarr.demoappgym.model.enums.Role;
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
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest request,
            Authentication authentication) {
        Role requestedRole = request.getRole();
        boolean isAdmin = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (requestedRole != null && requestedRole != Role.USER && !isAdmin) {
            throw new AccessDeniedException("Solo un administrador puede asignar el rol solicitado");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("La contraseña es obligatoria para crear un usuario");
        }
        if (request.getDocumentNumber() == null || request.getDocumentNumber().isBlank()) {
            throw new BusinessException("La cédula de identidad es obligatoria para crear un usuario");
        }

        User user = new User();
        applyRequest(user, request);
        user.setRole(requestedRole == null ? Role.USER : requestedRole);
        return ResponseEntity.ok(toResponse(userService.save(user), false));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserRequest request) {
        return userService.findById(id)
                .map(existing -> {
                    applyRequest(existing, request);
                    return ResponseEntity.ok(toResponse(userService.save(existing), false));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUser() {
        return ResponseEntity.ok(userService.findAll().stream()
                .map(user -> toResponse(user, false)).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(toResponse(user, false)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyRequest(User user, UserRequest request) {
        user.setName(request.getName());
        user.setAddress(request.getAddress());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(request.getPassword());
        }
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDocumentNumber(request.getDocumentNumber());
        if (request.getPhotoData() != null) {
            user.setPhotoData(request.getPhotoData());
        }
        user.setMail(request.getMail());
        user.setPhysicalConditionReport(request.getPhysicalConditionReport());
        user.setRole(request.getRole());
    }

    private UserResponse toResponse(User user, boolean includePassword) {
        return new UserResponse(user.getCodeUser(), user.getName(), user.getAddress(),
                user.getPhoneNumber(), user.getMail(), user.getDocumentNumber(),
                includePassword ? user.getPassword() : null, user.getPhotoData(), user.getPhysicalConditionReport(),
                user.getRole() == null ? null : user.getRole().name());
    }
}
