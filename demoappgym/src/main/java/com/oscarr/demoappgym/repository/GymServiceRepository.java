package com.oscarr.demoappgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oscarr.demoappgym.model.entity.GymService;

public interface GymServiceRepository extends JpaRepository<GymService,Long> {

}
