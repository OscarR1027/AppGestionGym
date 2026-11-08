package com.oscarr.demoappgym.security;

import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.oscarr.demoappgym.model.enums.Role;
import com.oscarr.demoappgym.repository.UserRepository;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/index.html", "/favicon.ico", "/assets/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/products/low-stock").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/**",
                                "/api/coachs/**",
                                "/api/gymservice/**",
                                "/api/typeplan/**")
                        .hasAnyRole(Role.ADMIN.name(), Role.USER.name(), Role.COACH.name())
                        .requestMatchers("/api/**").hasRole(Role.ADMIN.name())
                        .anyRequest().authenticated())
                .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(
                        (request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Credenciales no válidas\"}");
                        }))
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:4200,http://localhost:5173}") String allowedOriginsValue) {
        List<String> allowedOrigins = Arrays.stream(allowedOriginsValue.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .distinct()
                .toList();
        if (allowedOrigins.isEmpty()) {
            allowedOrigins = List.of("http://localhost:3000", "http://localhost:4200", "http://localhost:5173");
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return mail -> userRepository.findByMail(mail)
                .map(user -> {
                    if (user.getRole() == null) {
                        throw new UsernameNotFoundException("El usuario no tiene un rol asignado");
                    }
                    return User.withUsername(user.getMail())
                            .password(user.getPassword())
                            .roles(user.getRole().name())
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new LegacyAwarePasswordEncoder();
    }
}
