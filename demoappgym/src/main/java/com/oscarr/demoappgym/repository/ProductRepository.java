package com.oscarr.demoappgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import com.oscarr.demoappgym.model.entity.Product;

public interface ProductRepository extends JpaRepository<Product,Long> {

    List<Product> findByAmountLessThan(Long amount);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.codeProducts = :productId")
    Optional<Product> findByIdForUpdate(@Param("productId") Long productId);
}
