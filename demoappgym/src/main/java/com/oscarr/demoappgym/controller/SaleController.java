package com.oscarr.demoappgym.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oscarr.demoappgym.model.dto.MonthlyRevenueResponse;
import com.oscarr.demoappgym.model.dto.SaleProductResponse;
import com.oscarr.demoappgym.model.dto.SaleRequest;
import com.oscarr.demoappgym.model.dto.SaleResponse;
import com.oscarr.demoappgym.model.entity.Sale;
import com.oscarr.demoappgym.service.SaleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<SaleResponse> createSale(@Valid @RequestBody SaleRequest request) {
        return ResponseEntity.ok(toResponse(saleService.createWithProducts(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleResponse> updateSale(
            @PathVariable Long id,
            @Valid @RequestBody SaleRequest request) {
        return ResponseEntity.ok(toResponse(saleService.updateWithProducts(id, request)));
    }

    @GetMapping
    public ResponseEntity<List<SaleResponse>> getAllSale() {
        return ResponseEntity.ok(saleService.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/monthly-revenue")
    public ResponseEntity<MonthlyRevenueResponse> getCurrentMonthRevenue() {
        return ResponseEntity.ok(saleService.getCurrentMonthRevenue());
    }

    @GetMapping("/{id}/factura")
    public ResponseEntity<SaleResponse> getInvoice(@PathVariable Long id) {
        return saleService.findById(id)
                .map(sale -> ResponseEntity.ok(toResponse(sale)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getSaleById(@PathVariable Long id) {
        return getInvoice(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        saleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private SaleResponse toResponse(Sale sale) {
        List<SaleProductResponse> products = saleService.findProductsBySaleId(sale.getIdBillSale()).stream()
                .map(saleProduct -> new SaleProductResponse(
                        saleProduct.getProduct().getCode_Products(),
                        saleProduct.getProduct().getName_Product(),
                        saleProduct.getAmount(),
                        saleProduct.getProduct().getValue(),
                        saleProduct.getTotalValue()))
                .toList();
        return new SaleResponse(
                sale.getIdBillSale(),
                sale.getUser().getCodeUser(),
                sale.getUser().getName(),
                sale.getSaleDate(),
                sale.getTotalValue(),
                products);
    }
}
