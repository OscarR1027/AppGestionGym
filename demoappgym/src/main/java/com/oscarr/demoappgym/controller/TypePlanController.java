package com.oscarr.demoappgym.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oscarr.demoappgym.model.entity.TypePlan;
import com.oscarr.demoappgym.service.TypePlanService;

@RestController 
@RequestMapping  ("/api/typeplan/")
public class TypePlanController {
    private final TypePlanService typePlanService;  

    public TypePlanController(TypePlanService typePlanService){
        this.typePlanService=typePlanService;
    }

    @PostMapping 
    public ResponseEntity<TypePlan> createTypePlan(@RequestBody TypePlan typePlan){
        TypePlan newTypePlan=typePlanService.save(typePlan);
            return  ResponseEntity.ok(newTypePlan);
    }
    
    @GetMapping 
public ResponseEntity<List<TypePlan>> getAllTypePlan(){
        List<TypePlan> typePlans= typePlanService.findAll();
            return ResponseEntity.ok(typePlans);
    }

    @GetMapping ("/{id}/")
    public ResponseEntity<TypePlan> getTypePlanById(@PathVariable Long id){
        Optional<TypePlan> typePlan= typePlanService.findById(id);
            if(typePlan.isPresent()){
                return ResponseEntity.ok(typePlan.get());
            }
            else{
                return ResponseEntity.notFound().build();
            }
    }
    
    @DeleteMapping ("/{id}/")
    public ResponseEntity<Void> deleteTypePlan(@PathVariable Long id){
        typePlanService.deleteById(id);
            return ResponseEntity.noContent().build();
    }
}
