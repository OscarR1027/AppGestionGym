package com.oscarr.demoappgym.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oscarr.demoappgym.model.entity.Coach;
import com.oscarr.demoappgym.repository.CoachRepository;
@Service 
public class CoachService implements GenericCrud<Coach,Long> {

private final CoachRepository coachRepository;

public CoachService(CoachRepository coachRepository){
    this.coachRepository=coachRepository;
    
}
@Override 
public Coach save(Coach coach){
    return coachRepository.save(coach);
}
@Override 
    public List<Coach> findAll(){
        return coachRepository.findAll();
    }
    @Override 
    public Optional<Coach> findById(Long id){
        return coachRepository.findById(id);
    }
    @Override 
    public void deleteById(Long id){
        coachRepository.deleteById(id);
    }
}
