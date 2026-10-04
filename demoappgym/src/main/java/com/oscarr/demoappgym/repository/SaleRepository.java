package com.oscarr.demoappgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oscarr.demoappgym.model.entity.Sale;
import java.time.LocalDate;

public interface SaleRepository extends JpaRepository<Sale,Long>{

    @Query("SELECT COALESCE(SUM(s.totalValue), 0.0) FROM Sale s "
            + "WHERE s.saleDate >= :startDate AND s.saleDate < :endDate")
    Double sumTotalValueBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
