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

import com.oscarr.demoappgym.model.entity.Coach;

import com.oscarr.demoappgym.service.CoachService;



@RestController 
@RequestMapping ("/api/coachs/")
public class CoachController {

    private final CoachService coachService;

    public CoachController(CoachService coachService){
        this.coachService=coachService;
    }

    @PostMapping 
    public ResponseEntity<Coach> createCoach(@RequestBody Coach coach){
        Coach newCoach= coachService.save(coach);
        return ResponseEntity.ok(newCoach);
    }

    @GetMapping 
    public ResponseEntity<List<Coach>> getAllCoach(){
        List<Coach> coachs= coachService.findAll();
            return ResponseEntity.ok(coachs);
    }

    @GetMapping ("/{id}")

    public ResponseEntity<Coach> getCoachById(@PathVariable Long id){

        Optional<Coach> coach= coachService.findById(id);
        if(coach.isPresent()){
            return ResponseEntity.ok(coach.get());
        }
        else{
            return ResponseEntity.notFound().build();
        }
    
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoach(@PathVariable Long id) {
        coachService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
