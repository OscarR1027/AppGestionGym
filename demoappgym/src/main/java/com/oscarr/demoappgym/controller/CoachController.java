package com.oscarr.demoappgym.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oscarr.demoappgym.model.dto.CoachRequest;
import com.oscarr.demoappgym.model.dto.CoachResponse;
import com.oscarr.demoappgym.model.entity.Coach;
import com.oscarr.demoappgym.service.CoachService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/coachs")
public class CoachController {

    private final CoachService coachService;

    public CoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    @PostMapping
    public ResponseEntity<CoachResponse> createCoach(@Valid @RequestBody CoachRequest request) {
        Coach coach = new Coach();
        coach.setNameCoach(request.getNameCoach());
        coach.setPhotoData(request.getPhotoData());
        return ResponseEntity.ok(toResponse(coachService.save(coach)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoachResponse> updateCoach(
            @PathVariable("id") Long id,
            @Valid @RequestBody CoachRequest request) {
        return coachService.findById(id)
                .map(coach -> {
                    coach.setNameCoach(request.getNameCoach());
                    if (request.hasPhotoData()) {
                        coach.setPhotoData(request.getPhotoData());
                    }
                    return ResponseEntity.ok(toResponse(coachService.save(coach)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CoachResponse>> getAllCoach() {
        return ResponseEntity.ok(coachService.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoachResponse> getCoachById(@PathVariable("id") Long id) {
        return coachService.findById(id)
                .map(coach -> ResponseEntity.ok(toResponse(coach)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoach(@PathVariable("id") Long id) {
        coachService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private CoachResponse toResponse(Coach coach) {
        return new CoachResponse(coach.getIdCoach(), coach.getNameCoach(), coach.getPhotoData());
    }
}
