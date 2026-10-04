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

import com.oscarr.demoappgym.model.dto.AffiliationCoachResponse;
import com.oscarr.demoappgym.model.dto.AffiliationRequest;
import com.oscarr.demoappgym.model.dto.AffiliationResponse;
import com.oscarr.demoappgym.model.entity.Affiliation;
import com.oscarr.demoappgym.model.entity.TypePlan;
import com.oscarr.demoappgym.model.entity.User;
import com.oscarr.demoappgym.service.AffiliationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/affiliation")
public class AffiliationController {

    private final AffiliationService affiliationService;

    public AffiliationController(AffiliationService affiliationService) {
        this.affiliationService = affiliationService;
    }

    @PostMapping
    public ResponseEntity<AffiliationResponse> createAffiliation(
            @Valid @RequestBody AffiliationRequest request) {
        Affiliation affiliation = fromRequest(request);
        Affiliation saved = affiliationService.saveWithCoaches(affiliation, request.getCoaches());
        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AffiliationResponse> updateAffiliation(
            @PathVariable Long id,
            @Valid @RequestBody AffiliationRequest request) {
        Affiliation affiliation = fromRequest(request);
        affiliation.setNumberAffiliation(id);
        Affiliation saved = affiliationService.saveWithCoaches(affiliation, request.getCoaches());
        return ResponseEntity.ok(toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<AffiliationResponse>> getAllAffiliation() {
        return ResponseEntity.ok(affiliationService.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<AffiliationResponse>> getExpiringAffiliations() {
        return ResponseEntity.ok(affiliationService.findExpiringWithinThreeDays().stream()
                .map(this::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AffiliationResponse> getAffiliationById(@PathVariable Long id) {
        return affiliationService.findById(id)
                .map(affiliation -> ResponseEntity.ok(toResponse(affiliation)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAffiliation(@PathVariable Long id) {
        affiliationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Affiliation fromRequest(AffiliationRequest request) {
        Affiliation affiliation = new Affiliation();
        User user = new User();
        user.setCodeUser(request.getUserId());
        TypePlan typePlan = new TypePlan();
        typePlan.setIdTypePlan(request.getTypePlanId());
        affiliation.setUser(user);
        affiliation.setTypePlan(typePlan);
        affiliation.setAffiliationDate(request.getAffiliationDate());
        affiliation.setEndAffiliation(request.getEndAffiliation());
        return affiliation;
    }

    private AffiliationResponse toResponse(Affiliation affiliation) {
        List<AffiliationCoachResponse> coaches = affiliationService
                .findCoachAssignments(affiliation.getNumberAffiliation()).stream()
                .map(assignment -> new AffiliationCoachResponse(
                        assignment.getCoach().getIdCoach(),
                        assignment.getCoach().getNameCoach(),
                        assignment.getStartdate(),
                        assignment.getEndDate()))
                .toList();
        AffiliationResponse response = new AffiliationResponse();
        response.setId(affiliation.getNumberAffiliation());
        response.setUserId(affiliation.getUser().getCodeUser());
        response.setUserName(affiliation.getUser().getName());
        response.setAffiliationDate(affiliation.getAffiliationDate());
        response.setEndAffiliation(affiliation.getEndAffiliation());
        response.setTypePlanId(affiliation.getTypePlan().getIdTypePlan());
        response.setTypePlanName(affiliation.getTypePlan().getNamePlan());
        response.setCoaches(coaches);
        return response;
    }
}
