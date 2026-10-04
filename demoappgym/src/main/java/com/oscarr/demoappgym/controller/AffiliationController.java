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

import com.oscarr.demoappgym.model.entity.Affiliation;
import com.oscarr.demoappgym.service.AffiliationService;

@RestController 
@RequestMapping ("/api/affiliation/")
public class AffiliationController {
    private final AffiliationService affiliationService;

    public AffiliationController(AffiliationService affiliationService){
        this.affiliationService=affiliationService;
    }

    @PostMapping 
    public ResponseEntity<Affiliation> createAffiliation(@RequestBody Affiliation affiliation){
        Affiliation newAffiliation=affiliationService.save(affiliation);
            return  ResponseEntity.ok(newAffiliation);
    }
    
    @GetMapping 
    public ResponseEntity<List<Affiliation>> getAllAffiliation(){
        List<Affiliation> affiliations= affiliationService.findAll();
            return ResponseEntity.ok(affiliations);
    }

    @GetMapping ("/{id}/")
    public ResponseEntity<Affiliation> getAffiliationById(@PathVariable Long id){
        Optional<Affiliation> affiliation= affiliationService.findById(id);
            if(affiliation.isPresent()){
                return ResponseEntity.ok(affiliation.get());
            }
            else{
                return ResponseEntity.notFound().build();
            }
    }

    @DeleteMapping ("/{id}/")
    public ResponseEntity<Void> deleteAffiliation(@PathVariable Long id){
        affiliationService.deleteById(id);
            return ResponseEntity.noContent().build();
    }
    

}
