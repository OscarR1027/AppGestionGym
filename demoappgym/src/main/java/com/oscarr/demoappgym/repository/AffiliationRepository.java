package com.oscarr.demoappgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oscarr.demoappgym.model.entity.Affiliation;

public interface AffiliationRepository extends JpaRepository<Affiliation,Long> {

}
