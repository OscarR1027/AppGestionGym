package com.oscarr.demoappgym.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity 
@Table (name="Sale_Product")
public class SaleProduct {

@EmbeddedId 
private SaleProductId id = new SaleProductId();

@ManyToOne 
@MapsId ("codeProduct")
@JoinColumn (name="codeProduct",nullable = false)
private Product product;

@ManyToOne 
@MapsId ("idBillSale")
@JoinColumn (name="idBillSale",nullable = false)
private Sale sale;

@Column (name="Amount",nullable = false)
private Long amount;

@Column (name="Total_Value",precision = 12,nullable = false)
private Double totalValue;

public SaleProduct (){}

public SaleProduct(SaleProductId id, Product product, Sale sale, Long amount, Double totalValue) {
    this.id = id;
    this.product = product;
    this.sale = sale;
    this.amount = amount;
    this.totalValue = totalValue;
}

public SaleProductId getId() {
    return id;
}

public void setId(SaleProductId id) {
    this.id = id;
}

public Product getProduct() {
    return product;
}

public void setProduct(Product product) {
    this.product = product;
}

public Sale getSale() {
    return sale;
}

public void setSale(Sale sale) {
    this.sale = sale;
}

public Long getAmount() {
    return amount;
}

public void setAmount(Long amount) {
    this.amount = amount;
}

public Double getTotalValue() {
    return totalValue;
}

public void setTotalValue(Double totalValue) {
    this.totalValue = totalValue;
}


}
