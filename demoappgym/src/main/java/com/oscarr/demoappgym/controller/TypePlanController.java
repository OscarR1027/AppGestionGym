package com.oscarr.demoappgym.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oscarr.demoappgym.exception.ResourceNotFoundException;
import com.oscarr.demoappgym.model.dto.GymServiceResponse;
import com.oscarr.demoappgym.model.dto.TypePlanRequest;
import com.oscarr.demoappgym.model.dto.TypePlanResponse;
import com.oscarr.demoappgym.model.entity.GymService;
import com.oscarr.demoappgym.model.entity.TypePlan;
import com.oscarr.demoappgym.service.GymServiceService;
import com.oscarr.demoappgym.service.TypePlanService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/typeplan")
public class TypePlanController {

    private final TypePlanService typePlanService;
    private final GymServiceService gymServiceService;

    public TypePlanController(TypePlanService typePlanService, GymServiceService gymServiceService) {
        this.typePlanService = typePlanService;
        this.gymServiceService = gymServiceService;
    }

    @PostMapping
    public ResponseEntity<TypePlanResponse> createTypePlan(@Valid @RequestBody TypePlanRequest request) {
        TypePlan typePlan = fromRequest(request);
        return ResponseEntity.ok(toResponse(typePlanService.save(typePlan)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TypePlanResponse> updateTypePlan(
            @PathVariable("id") Long id,
            @Valid @RequestBody TypePlanRequest request) {
        Optional<TypePlan> existing = typePlanService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TypePlan typePlan = fromRequest(request);
        typePlan.setIdTypePlan(id);
        return ResponseEntity.ok(toResponse(typePlanService.save(typePlan)));
    }

    @GetMapping
    public ResponseEntity<List<TypePlanResponse>> getAllTypePlan() {
        return ResponseEntity.ok(typePlanService.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TypePlanResponse> getTypePlanById(@PathVariable("id") Long id) {
        return typePlanService.findById(id)
                .map(typePlan -> ResponseEntity.ok(toResponse(typePlan)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTypePlan(@PathVariable("id") Long id) {
        typePlanService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private TypePlan fromRequest(TypePlanRequest request) {
        TypePlan typePlan = new TypePlan();
        typePlan.setNamePlan(request.getNamePlan());
        typePlan.setValue(request.getValue());
        List<GymService> services = request.getServiceIds() == null
                ? List.of()
                : request.getServiceIds().stream()
                        .map(id -> gymServiceService.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "No se encontró el servicio con ID " + id)))
                        .toList();
        typePlan.setServices(services);
        return typePlan;
    }

    private TypePlanResponse toResponse(TypePlan typePlan) {
        List<GymServiceResponse> services = typePlan.getServices() == null
                ? List.of()
                : typePlan.getServices().stream()
                        .map(service -> new GymServiceResponse(service.getIdService(), service.getServiceName()))
                        .toList();
        return new TypePlanResponse(typePlan.getIdTypePlan(), typePlan.getNamePlan(),
                typePlan.getValue(), services);
    }
}
