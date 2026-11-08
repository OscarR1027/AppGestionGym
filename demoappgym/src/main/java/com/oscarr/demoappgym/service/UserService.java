package com.oscarr.demoappgym.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.oscarr.demoappgym.exception.BusinessException;
import com.oscarr.demoappgym.model.entity.User;
import com.oscarr.demoappgym.repository.AffiliationRepository;
import com.oscarr.demoappgym.repository.UserRepository;

@Service 
public class UserService implements GenericCrud<User,Long> {

    private final UserRepository userRepository;
    private final AffiliationRepository affiliationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService (UserRepository userRepository, AffiliationRepository affiliationRepository,
            PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.affiliationRepository = affiliationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override 
    public User save(User user){
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BusinessException("La contraseña es obligatoria");
        }
        String password = user.getPassword();
        if (!password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
            user.setPassword(passwordEncoder.encode(password));
        }
        return userRepository.save(user);
    }

    @Override 
    public List<User> findAll(){
        return userRepository.findAll();
    }

    @Override 
    public Optional<User> findById (Long id){
        return userRepository.findById(id);   
    }

    @Override 
    public void deleteById(Long id){
        if (affiliationRepository.existsByUser_CodeUser(id)) {
            throw new BusinessException("No se puede eliminar este usuario porque tiene afiliaciones asociadas. Revisa primero las afiliaciones del cliente.");
        }
        userRepository.deleteById(id);
    }
}
