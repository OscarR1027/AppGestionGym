package com.oscarr.demoappgym.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oscarr.demoappgym.model.entity.Sale;
import com.oscarr.demoappgym.repository.SaleProductRepository;
import com.oscarr.demoappgym.repository.SaleRepository;

import org.springframework.transaction.annotation.Transactional;

@Service 
public class SaleService implements GenericCrud<Sale,Long> {
    
    private final SaleRepository saleRepository;
    private final SaleProductRepository saleProductRepository;
//TODO: Falta implementar guardar venta con productos
    public SaleService(SaleRepository saleRepository, SaleProductRepository saleProductRepository){
        this.saleRepository=saleRepository;
        this.saleProductRepository=saleProductRepository;
    }
    @Override 
    @Transactional 
    public Sale save(Sale sale){
        return saleRepository.save(sale);
    }

    @Override 
    @Transactional(readOnly = true)
    public List<Sale> findAll(){
        return saleRepository.findAll();
    }
    @Override 
    @Transactional(readOnly = true)
    public Optional<Sale> findById(Long id){
        return saleRepository.findById(id);
    }
    @Override 
    @Transactional 
    public void deleteById(Long id){
        saleRepository.deleteById(id);
    }
}
