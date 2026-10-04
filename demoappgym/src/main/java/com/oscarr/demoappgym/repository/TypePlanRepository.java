package com.oscarr.demoappgym.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import com.oscarr.demoappgym.model.entity.TypePlan;

public interface TypePlanRepository extends JpaRepository<TypePlan,Long> {

    @Override
    @EntityGraph(attributePaths = "services")
    List<TypePlan> findAll();

    @Override
    @EntityGraph(attributePaths = "services")
    Optional<TypePlan> findById(Long id);
}
