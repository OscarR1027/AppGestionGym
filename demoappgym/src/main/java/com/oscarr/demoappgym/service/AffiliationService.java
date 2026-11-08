package com.oscarr.demoappgym.service;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oscarr.demoappgym.exception.BusinessException;
import com.oscarr.demoappgym.exception.ResourceNotFoundException;
import com.oscarr.demoappgym.model.dto.AffiliationCoachRequest;
import com.oscarr.demoappgym.model.entity.Affiliation;
import com.oscarr.demoappgym.model.entity.AffiliationCoach;
import com.oscarr.demoappgym.model.entity.AffiliationCoachId;
import com.oscarr.demoappgym.model.entity.Coach;
import com.oscarr.demoappgym.repository.AffiliationCoachRepository;
import com.oscarr.demoappgym.repository.AffiliationRepository;
import com.oscarr.demoappgym.repository.CoachRepository;
import com.oscarr.demoappgym.repository.TypePlanRepository;
import com.oscarr.demoappgym.repository.UserRepository;

@Service 
public class AffiliationService implements GenericCrud<Affiliation,Long> {
    
    private final AffiliationRepository affiliationRepository;
    private final AffiliationCoachRepository affiliationCoachRepository;
    private final UserRepository userRepository;
    private final TypePlanRepository typePlanRepository;
    private final CoachRepository coachRepository;
    
    public AffiliationService(
            AffiliationRepository affiliationRepository,
            AffiliationCoachRepository affiliationCoachRepository,
            UserRepository userRepository,
            TypePlanRepository typePlanRepository,
            CoachRepository coachRepository) {
        this.affiliationRepository=affiliationRepository;
        this.affiliationCoachRepository = affiliationCoachRepository;
        this.userRepository = userRepository;
        this.typePlanRepository = typePlanRepository;
        this.coachRepository = coachRepository;
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
    @Transactional
    public void deleteById(Long id){
        affiliationRepository.findById(id).ifPresent(affiliation ->
                affiliationCoachRepository.deleteAll(
                        affiliationCoachRepository.findAllByAffiliationId(affiliation.getNumberAffiliation())));
        affiliationRepository.deleteById(id);
    }

    @Transactional
    public boolean cancelInvoice(Long id) {
        Affiliation affiliation = affiliationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la afiliación con ID " + id));
        if (affiliation.isRenewal()) {
            affiliation.setInvoiceCancelled(true);
            return true;
        }
        deleteById(id);
        return false;
    }

    @Transactional
    public Affiliation saveWithCoaches(Affiliation affiliation, List<AffiliationCoachRequest> coachRequests) {
        if (affiliation == null || affiliation.getUser() == null
                || affiliation.getUser().getCodeUser() == null
                || affiliation.getTypePlan() == null || affiliation.getTypePlan().getIdTypePlan() == null
                || affiliation.getEndAffiliation() == null) {
            throw new BusinessException("El usuario, el plan y la fecha de vencimiento son obligatorios");
        }
        if (affiliation.getNumberAffiliation() != null
                && !affiliationRepository.existsById(affiliation.getNumberAffiliation())) {
            throw new ResourceNotFoundException(
                    "No se encontró la afiliación con ID " + affiliation.getNumberAffiliation());
        }
        if (!"CASH".equals(affiliation.getPaymentMethod())
                && !"CARD".equals(affiliation.getPaymentMethod())
                && !"TRANSFER".equals(affiliation.getPaymentMethod())) {
            throw new BusinessException("Selecciona efectivo, tarjeta o transferencia como método de pago");
        }

        LocalDate affiliationDate = affiliation.getAffiliationDate() == null
                ? LocalDate.now()
                : affiliation.getAffiliationDate();
        if (affiliation.getEndAffiliation().isBefore(affiliationDate)) {
            throw new BusinessException("La fecha de vencimiento no puede ser anterior a la fecha de afiliación");
        }
        affiliation.setAffiliationDate(affiliationDate);
        affiliation.setUser(userRepository.findById(affiliation.getUser().getCodeUser())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario con ID " + affiliation.getUser().getCodeUser())));
        affiliation.setTypePlan(typePlanRepository.findById(affiliation.getTypePlan().getIdTypePlan())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el plan con ID " + affiliation.getTypePlan().getIdTypePlan())));
        if (affiliation.getNumberAffiliation() == null) {
            affiliation.setRenewal(affiliationRepository.existsByUser_CodeUser(affiliation.getUser().getCodeUser()));
            affiliation.setPaidValue(affiliation.getTypePlan().getValue().doubleValue());
        } else {
            Affiliation existing = affiliationRepository.findById(affiliation.getNumberAffiliation())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró la afiliación con ID " + affiliation.getNumberAffiliation()));
            affiliation.setRenewal(existing.isRenewal());
            affiliation.setInvoiceCancelled(existing.isInvoiceCancelled());
            if (affiliation.getPaidValue() == null) {
                affiliation.setPaidValue(existing.getPaidValue() == null
                        ? affiliation.getTypePlan().getValue().doubleValue()
                        : existing.getPaidValue());
            }
        }

        if (affiliation.getNumberAffiliation() != null) {
            affiliationCoachRepository.deleteAll(
                    affiliationCoachRepository.findAllByAffiliationId(affiliation.getNumberAffiliation()));
        }
        Affiliation savedAffiliation = affiliationRepository.save(affiliation);
        List<AffiliationCoach> assignments = new ArrayList<>();
        HashSet<Long> assignedCoachIds = new HashSet<>();
        if (coachRequests != null) {
            for (AffiliationCoachRequest coachRequest : coachRequests) {
                if (coachRequest == null || coachRequest.getCoachId() == null) {
                    throw new BusinessException("Cada coach debe tener un ID válido");
                }
                Long coachId = coachRequest.getCoachId();
                if (!assignedCoachIds.add(coachId)) {
                    throw new BusinessException("El coach con ID " + coachId + " está repetido");
                }
                LocalDate startDate = coachRequest.getStartDate() == null
                        ? affiliationDate
                        : coachRequest.getStartDate();
                LocalDate endDate = coachRequest.getEndDate() == null
                        ? savedAffiliation.getEndAffiliation()
                        : coachRequest.getEndDate();
                if (startDate.isBefore(affiliationDate)
                        || endDate.isAfter(savedAffiliation.getEndAffiliation())
                        || endDate.isBefore(startDate)) {
                    throw new BusinessException(
                            "Las fechas de asignación del coach deben estar dentro del período de afiliación");
                }

                Coach coach = coachRepository.findById(coachId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No se encontró el coach con ID " + coachId));
                AffiliationCoach assignment = new AffiliationCoach();
                assignment.setId(new AffiliationCoachId(coachId, savedAffiliation.getNumberAffiliation()));
                assignment.setCoach(coach);
                assignment.setAffiliation(savedAffiliation);
                assignment.setStartdate(startDate);
                assignment.setEndDate(endDate);
                assignments.add(assignment);
            }
        }
        if (!assignments.isEmpty()) {
            affiliationCoachRepository.saveAll(assignments);
        }
        return savedAffiliation;
    }

    @Transactional(readOnly = true)
    public List<AffiliationCoach> findCoachAssignments(Long affiliationId) {
        return affiliationCoachRepository.findAllByAffiliationId(affiliationId);
    }

    public List<Affiliation> findExpiringWithinThreeDays() {
        LocalDate today = LocalDate.now();
        return affiliationRepository.findByEndAffiliationBetween(today, today.plusDays(3)).stream()
                .filter(affiliation -> !affiliation.isInvoiceCancelled())
                .toList();
    }

}
