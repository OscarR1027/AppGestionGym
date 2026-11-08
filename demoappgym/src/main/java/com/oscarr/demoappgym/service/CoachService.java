package com.oscarr.demoappgym.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oscarr.demoappgym.exception.BusinessException;
import com.oscarr.demoappgym.model.entity.Coach;
import com.oscarr.demoappgym.repository.AffiliationCoachRepository;
import com.oscarr.demoappgym.repository.CoachRepository;
@Service 
public class CoachService implements GenericCrud<Coach,Long> {

private final CoachRepository coachRepository;
private final AffiliationCoachRepository affiliationCoachRepository;

public CoachService(CoachRepository coachRepository, AffiliationCoachRepository affiliationCoachRepository){
    this.coachRepository = coachRepository;
    this.affiliationCoachRepository = affiliationCoachRepository;
    
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
        if (affiliationCoachRepository.existsByCoach_IdCoach(id)) {
            throw new BusinessException("No se puede eliminar este entrenador porque está asociado a uno o más alumnos. Desvincúlalo de sus afiliaciones primero.");
        }
        coachRepository.deleteById(id);
    }
}
