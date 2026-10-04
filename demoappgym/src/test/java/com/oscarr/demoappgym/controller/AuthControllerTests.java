package com.oscarr.demoappgym.controller;

import com.oscarr.demoappgym.model.entity.User;
import com.oscarr.demoappgym.model.enums.Role;
import com.oscarr.demoappgym.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AuthController authController = new AuthController(userRepository, passwordEncoder);

    @Test
    void authenticatesAgainstUserStoredInJpaRepository() {
        User admin = new User();
        admin.setCodeUser(7L);
        admin.setName("Administradora");
        admin.setMail("admin@gym.local");
        admin.setPassword("$2b$hash");
        admin.setRole(Role.ADMIN);
        when(userRepository.findByMail("admin@gym.local")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("clave-valida", "$2b$hash")).thenReturn(true);

        var response = authController.login(new AuthController.LoginRequest(" admin@gym.local ", "clave-valida"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Role.ADMIN, response.getBody().role());
        assertEquals(7L, response.getBody().id());
        verify(userRepository).findByMail("admin@gym.local");
        verify(passwordEncoder).matches("clave-valida", "$2b$hash");
    }

    @Test
    void rejectsIncorrectPassword() {
        User user = new User();
        user.setMail("admin@gym.local");
        user.setPassword("plain-or-bcrypt");
        when(userRepository.findByMail("admin@gym.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("incorrecta", "plain-or-bcrypt")).thenReturn(false);

        var response = authController.login(new AuthController.LoginRequest("admin@gym.local", "incorrecta"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
