package com.oscarr.demoappgym.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oscarr.demoappgym.model.entity.Affiliation;
import com.oscarr.demoappgym.repository.AffiliationCoachRepository;
import com.oscarr.demoappgym.repository.AffiliationRepository;

@Service 
public class AffiliationService implements GenericCrud<Affiliation,Long> {
    
    private final AffiliationRepository affiliationRepository;
    private final AffiliationCoachRepository affiliationCoachRepository;
    //TODO: falta implementar el metodo de guardar afiliacion con coach
    
    public AffiliationService(AffiliationRepository affiliationRepository, AffiliationCoachRepository affiliationCoachRepository){
        this.affiliationRepository=affiliationRepository;
        this.affiliationCoachRepository=affiliationCoachRepository;
    }
    @Override 
    public Affiliation save(Affiliation affiliation){
        return affiliationRepository.save(affiliation);
    }
    @Override 
    public List<Affiliation> findAll(){
        return affiliationRepository.findAll();
    }
    @Override 
    public Optional<Affiliation> findById(Long id){
        return affiliationRepository.findById(id);
    }
    @Override
    public void deleteById(Long id){
        affiliationRepository.deleteById(id);
    }

}
