package com.oscarr.demoappgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oscarr.demoappgym.model.entity.Product;

public interface ProductRepository extends JpaRepository<Product,Long> {

}
