package com.oscarr.demoappgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

import com.oscarr.demoappgym.model.entity.Affiliation;

public interface AffiliationRepository extends JpaRepository<Affiliation,Long> {

    List<Affiliation> findByEndAffiliationBetween(LocalDate startDate, LocalDate endDate);

    boolean existsByUser_CodeUser(Long userId);
}
