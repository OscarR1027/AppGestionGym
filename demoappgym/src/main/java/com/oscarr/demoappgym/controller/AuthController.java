package com.oscarr.demoappgym.controller;

import com.oscarr.demoappgym.model.entity.User;
import com.oscarr.demoappgym.model.enums.Role;
import com.oscarr.demoappgym.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByMail(request.email().trim())
                .orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.getRole() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(new LoginResponse(
                user.getCodeUser(), user.getName(), user.getMail(), user.getRole()));
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record LoginResponse(Long id, String name, String email, Role role) {
    }
}
