package com.oscarr.demoappgym.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oscarr.demoappgym.exception.BusinessException;
import com.oscarr.demoappgym.model.entity.Coach;
import com.oscarr.demoappgym.model.entity.User;
import com.oscarr.demoappgym.model.enums.Role;
import com.oscarr.demoappgym.repository.AffiliationRepository;
import com.oscarr.demoappgym.repository.CoachRepository;
import com.oscarr.demoappgym.repository.UserRepository;

@Service 
public class UserService implements GenericCrud<User,Long> {

    private final UserRepository userRepository;
    private final AffiliationRepository affiliationRepository;
    private final CoachRepository coachRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService (UserRepository userRepository, AffiliationRepository affiliationRepository,
            CoachRepository coachRepository,
            PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.affiliationRepository = affiliationRepository;
        this.coachRepository = coachRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override 
    @Transactional
    public User save(User user){
        boolean createCoach = user.getCodeUser() == null && user.getRole() == Role.COACH;
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            if (user.getRole() != Role.USER) {
                throw new BusinessException("La contraseña es obligatoria para entrenadores y administradores");
            }
            user.setPassword(UUID.randomUUID().toString());
        }
        String password = user.getPassword();
        if (!password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
            user.setPassword(passwordEncoder.encode(password));
        }
        User saved = userRepository.save(user);
        if (createCoach) {
            Coach coach = new Coach();
            coach.setNameCoach(saved.getName());
            coach.setPhotoData(saved.getPhotoData());
            coachRepository.save(coach);
        }
        return saved;
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
