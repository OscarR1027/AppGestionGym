package com.oscarr.demoappgym.model.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable 
public class SaleProductId implements Serializable {
// Creamos esta clase por el motivo de crear la llave compuesta entre Sale y Product con el metodo embeddable

private Long idBillSale;
private Long codeProduct;

public SaleProductId(){}

public SaleProductId(Long idBillSale, Long codeProduct) {
    this.idBillSale = idBillSale;
    this.codeProduct = codeProduct;
}

public Long getIdBillSale() {
    return idBillSale;
}

public void setIdBillSale(Long idBillSale) {
    this.idBillSale = idBillSale;
}

public Long getCodeProduct() {
    return codeProduct;
}

public void setCodeProduct(Long codeProduct) {
    this.codeProduct = codeProduct;
}

@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaleProductId that = (SaleProductId) o;
        return Objects.equals(idBillSale, that.idBillSale) && 
            Objects.equals(codeProduct, that.codeProduct);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idBillSale, codeProduct);
    }





}
