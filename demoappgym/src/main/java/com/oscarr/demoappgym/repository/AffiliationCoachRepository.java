package com.oscarr.demoappgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oscarr.demoappgym.model.entity.AffiliationCoach;
import com.oscarr.demoappgym.model.entity.AffiliationCoachId;
import java.util.List;


public interface AffiliationCoachRepository extends JpaRepository<AffiliationCoach,AffiliationCoachId> {

    @Query("SELECT ac FROM AffiliationCoach ac WHERE ac.affiliation.numberAffiliation = :affiliationId")
    List<AffiliationCoach> findAllByAffiliationId(@Param("affiliationId") Long affiliationId);
}
