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

import com.oscarr.demoappgym.model.dto.GymServiceRequest;
import com.oscarr.demoappgym.model.dto.GymServiceResponse;
import com.oscarr.demoappgym.model.entity.GymService;
import com.oscarr.demoappgym.service.GymServiceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/gymservice")
public class GymServiceController {

    private final GymServiceService gymServiceService;

    public GymServiceController(GymServiceService gymServiceService) {
        this.gymServiceService = gymServiceService;
    }

    @PostMapping
    public ResponseEntity<GymServiceResponse> createGymService(@Valid @RequestBody GymServiceRequest request) {
        GymService gymService = new GymService();
        gymService.setServiceName(request.getServiceName());
        return ResponseEntity.ok(toResponse(gymServiceService.save(gymService)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GymServiceResponse> updateGymService(
            @PathVariable Long id,
            @Valid @RequestBody GymServiceRequest request) {
        return gymServiceService.findById(id)
                .map(service -> {
                    service.setServiceName(request.getServiceName());
                    return ResponseEntity.ok(toResponse(gymServiceService.save(service)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<GymServiceResponse>> getAllGymService() {
        return ResponseEntity.ok(gymServiceService.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GymServiceResponse> getGymServiceById(@PathVariable Long id) {
        return gymServiceService.findById(id)
                .map(service -> ResponseEntity.ok(toResponse(service)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGymService(@PathVariable Long id) {
        gymServiceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private GymServiceResponse toResponse(GymService service) {
        return new GymServiceResponse(service.getIdService(), service.getServiceName());
    }
}
