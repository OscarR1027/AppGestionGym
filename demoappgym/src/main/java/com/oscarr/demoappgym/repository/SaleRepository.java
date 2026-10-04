package com.oscarr.demoappgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oscarr.demoappgym.model.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale,Long>{

}
