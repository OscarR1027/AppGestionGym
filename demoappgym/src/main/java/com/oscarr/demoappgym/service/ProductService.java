package com.oscarr.demoappgym.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oscarr.demoappgym.model.entity.Product;
import com.oscarr.demoappgym.repository.ProductRepository;

@Service 
public class ProductService implements GenericCrud<Product,Long> {
private final ProductRepository productRepository;
private static final long LOW_STOCK_THRESHOLD = 5L;

public ProductService(ProductRepository productRepository){

    this.productRepository=productRepository;
}


@Override 

public Product save(Product product){
return productRepository.save(product);
}
@Override 
public List<Product> findAll(){
    return productRepository.findAll();
}
@Override 
public Optional<Product> findById(Long id){
    return productRepository.findById(id);

}
@Override 
public void deleteById (Long id){
    productRepository.deleteById(id);
}

public List<Product> findLowStockProducts() {
    return productRepository.findByAmountLessThan(LOW_STOCK_THRESHOLD);
}
}
