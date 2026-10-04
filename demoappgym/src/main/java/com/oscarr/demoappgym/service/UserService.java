package com.oscarr.demoappgym.service;



import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.oscarr.demoappgym.model.entity.User;
import com.oscarr.demoappgym.repository.UserRepository;

@Service 
public class UserService implements GenericCrud<User,Long> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService (UserRepository userRepository, PasswordEncoder passwordEncoder){

        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
    }
@Override 
    public User save(User user){
    user.setPassword(passwordEncoder.encode(user.getPassword()));
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
        userRepository.deleteById(id);
    }
}
