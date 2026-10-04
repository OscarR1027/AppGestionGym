package com.oscarr.demoappgym.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name="Products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codeProducts;
    
    @Column(name="nameProduct",length=100,nullable = false)
    private String nameProduct;

    @Column (precision=12,nullable = false)
    private Double value;
    @Column (precision=5,nullable = true)
    private Long amount;

    public Product(){}

    public Product(Long codeProducts, String nameProduct, Double value, Long amount) {
        this.codeProducts = codeProducts;
        this.nameProduct = nameProduct;
        this.value = value;
        this.amount = amount;
    }

    public Long getCode_Products() {
        return codeProducts;
    }

    public void setCode_Products(Long codeProducts) {
        this.codeProducts = codeProducts;
    }

    public String getName_Product() {
        return nameProduct;
    }

    public void setName_Product(String nameProduct) {
        this.nameProduct = nameProduct;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    


}
