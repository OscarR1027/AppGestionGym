package com.oscarr.demoappgym.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import com.oscarr.demoappgym.model.entity.Product;
import com.oscarr.demoappgym.service.ProductService;

@RestController 
@RequestMapping ("/api/products")
public class ProductController {
    
    private final ProductService productService;

    public ProductController(ProductService productService){

        this.productService=productService;
        
    }

    @PostMapping 
    public ResponseEntity<Product> createProduct(@RequestBody Product product){
        Product newProduct=productService.save(product);
            return  ResponseEntity.ok(newProduct);
    }

    @GetMapping 
    public ResponseEntity<List<Product>> getAllProduct(){
        List<Product> products= productService.findAll();
            return ResponseEntity.ok(products);
}

    @GetMapping ("/{id}/")

    public ResponseEntity<Product> getProductById(@PathVariable Long id){
        Optional<Product> product= productService.findById(id);
            if(product.isPresent()){
                return ResponseEntity.ok(product.get());
            }
            else{
                return ResponseEntity.notFound().build();
            }

    }
    //TODO:Mejorar las excepciones en la busqueda

    @DeleteMapping ("/{id}/")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        productService.deleteById(id);
            return ResponseEntity.noContent().build();
    }


}
