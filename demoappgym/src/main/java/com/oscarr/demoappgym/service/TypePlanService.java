package com.oscarr.demoappgym.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oscarr.demoappgym.model.entity.TypePlan;
import com.oscarr.demoappgym.repository.TypePlanRepository;

@Service 
public class TypePlanService implements GenericCrud<TypePlan,Long> {

    private final TypePlanRepository typePlanRepository;

    public TypePlanService(TypePlanRepository typePlanRepository){
        this.typePlanRepository=typePlanRepository;
        
    }
    @Override 
    public TypePlan save(TypePlan typePlan){
        return typePlanRepository.save(typePlan);
    }
    @Override   
    public List<TypePlan> findAll(){
        return typePlanRepository.findAll();
    }
    @Override 
    public Optional<TypePlan> findById(Long id){
        return typePlanRepository.findById(id);
    }
    @Override 
    public void deleteById(Long id){
        typePlanRepository.deleteById(id);
    }

}
