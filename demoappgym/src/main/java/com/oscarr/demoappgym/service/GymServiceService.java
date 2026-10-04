package com.oscarr.demoappgym.service;


import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.oscarr.demoappgym.model.entity.GymService;
import com.oscarr.demoappgym.repository.GymServiceRepository;

@Service
public class GymServiceService implements GenericCrud<GymService,Long > {

    private final GymServiceRepository gymServiceRepository;

    public GymServiceService(GymServiceRepository gymServiceRepository) {
        this.gymServiceRepository = gymServiceRepository;
    }
    @Override 
    public GymService save(GymService gymService){
        return gymServiceRepository.save(gymService);
    }

    @Override
    public List<GymService> findAll(){
        return gymServiceRepository.findAll();
    }
    
    @Override
    public Optional<GymService> findById(Long id){
        return gymServiceRepository.findById(id);
    }
    
    @Override
    public void deleteById(Long id){
        gymServiceRepository.deleteById(id);
    }
}
