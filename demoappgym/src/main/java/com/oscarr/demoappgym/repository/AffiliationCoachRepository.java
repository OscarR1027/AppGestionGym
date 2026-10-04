package com.oscarr.demoappgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.oscarr.demoappgym.model.entity.AffiliationCoach;
import com.oscarr.demoappgym.model.entity.AffiliationCoachId;


public interface AffiliationCoachRepository extends JpaRepository<AffiliationCoach,AffiliationCoachId> {

}
