package com.oscarr.demoappgym.service;



import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.oscarr.demoappgym.model.entity.User;
import com.oscarr.demoappgym.repository.UserRepository;

@Service 
public class UserService implements GenericCrud<User,Long> {

    private final UserRepository userRepository;

    public UserService (UserRepository userRepository){

        this.userRepository=userRepository;
    }
@Override 
    public User save(User user){
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
