package com.oscarr.demoappgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oscarr.demoappgym.model.entity.SaleProduct;
import com.oscarr.demoappgym.model.entity.SaleProductId;
import java.util.List;


public interface SaleProductRepository extends JpaRepository<SaleProduct,SaleProductId> {

    @Query("SELECT sp FROM SaleProduct sp WHERE sp.sale.idBillSale = :saleId")
    List<SaleProduct> findAllBySaleId(@Param("saleId") Long saleId);
}
