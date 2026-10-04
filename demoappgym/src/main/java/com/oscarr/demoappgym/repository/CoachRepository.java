package com.oscarr.demoappgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oscarr.demoappgym.model.entity.Coach;

public interface CoachRepository extends JpaRepository<Coach,Long> {

}
