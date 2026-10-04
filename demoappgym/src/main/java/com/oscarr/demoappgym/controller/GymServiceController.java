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

import com.oscarr.demoappgym.model.entity.GymService;
import com.oscarr.demoappgym.service.GymServiceService;

@RestController 
@RequestMapping ("/api/gymservice/")
public class GymServiceController {

    private final GymServiceService gymServiceService;

    public GymServiceController(GymServiceService gymServiceService){
        this.gymServiceService=gymServiceService;
    }

    @PostMapping 
    public ResponseEntity<GymService> createProduct(@RequestBody GymService gymService){
        GymService newgGymService=gymServiceService.save(gymService);
            return  ResponseEntity.ok(newgGymService);
    }

    @GetMapping 
    public ResponseEntity<List<GymService>> getAllGymService(){
        List<GymService> gymServices= gymServiceService.findAll();
            return ResponseEntity.ok(gymServices);
    }

    @GetMapping ("/{id}/")
    public ResponseEntity<GymService> getGymServiceById(@PathVariable Long id){
        Optional<GymService> gymService= gymServiceService.findById(id);
            if(gymService.isPresent()){
                return ResponseEntity.ok(gymService.get()  );
            }
            else{
                return ResponseEntity.notFound().build();
            }
    }

    @DeleteMapping ("/{id}/")
    public ResponseEntity<Void> deleteGymService(@PathVariable Long id){
        
        gymServiceService.deleteById(id);
            return ResponseEntity.noContent().build();
    }
    
}
