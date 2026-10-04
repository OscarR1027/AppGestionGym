package com.oscarr.demoappgym.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oscarr.demoappgym.model.entity.Sale;
import com.oscarr.demoappgym.service.SaleService;

@RestController 
@RequestMapping ("/api/sales/")
public class SaleController {
    private final SaleService saleService;

    public SaleController(SaleService saleService){
        this.saleService=saleService;
    }

    @PostMapping 
    public ResponseEntity<Sale> createSale(@RequestBody Sale sale){
        Sale newSale=saleService.save(sale);
            return  ResponseEntity.ok(newSale);
    }

    @GetMapping 
    public ResponseEntity<List<Sale>> getAllSale(){
        List<Sale> sales= saleService.findAll();
            return ResponseEntity.ok(sales);
    }

    @GetMapping ("/{id}/")
    public ResponseEntity<Sale> getSaleById(@PathVariable Long id){
        Optional <Sale> sale= saleService.findById(id);
            if(sale.isPresent()){
                return ResponseEntity.ok(sale.get());
            }
            else{
                return ResponseEntity.notFound().build();
            }
    }

    @DeleteMapping ("/{id}/")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id){
        saleService.deleteById(id);
            return ResponseEntity.noContent().build();
    }

}
