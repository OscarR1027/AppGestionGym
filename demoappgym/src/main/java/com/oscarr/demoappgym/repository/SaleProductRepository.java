package com.oscarr.demoappgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oscarr.demoappgym.model.entity.SaleProduct;
import com.oscarr.demoappgym.model.entity.SaleProductId;


public interface SaleProductRepository extends JpaRepository<SaleProduct,SaleProductId> {

}
