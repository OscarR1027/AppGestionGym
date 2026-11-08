package com.oscarr.demoappgym.controller;

import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
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

    @Value("${INVOICE_DELETE_PASSWORD:}")
    private String invoiceDeletePassword;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<SaleResponse> createSale(@Valid @RequestBody SaleRequest request) {
        return ResponseEntity.ok(toResponse(saleService.createWithProducts(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleResponse> updateSale(
            @PathVariable("id") Long id,
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
    public ResponseEntity<SaleResponse> getInvoice(@PathVariable("id") Long id) {
        return saleService.findById(id)
                .map(sale -> ResponseEntity.ok(toResponse(sale)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getSaleById(@PathVariable("id") Long id) {
        return getInvoice(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> request) {
        validateInvoiceDeletePassword(request.get("password"));
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
                sale.getUser().getDocumentType() == null ? null : sale.getUser().getDocumentType().name(),
                sale.getUser().getDocumentNumber(),
                sale.getUser().getPhoneNumber(),
                sale.getSaleDate(),
                sale.getTotalValue(),
                sale.getPaymentMethod(),
                products);
    }

    private void validateInvoiceDeletePassword(String suppliedPassword) {
        if (invoiceDeletePassword == null || invoiceDeletePassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "La anulación de facturas no está configurada en el servidor");
        }
        if (suppliedPassword == null || !MessageDigest.isEqual(
                invoiceDeletePassword.getBytes(StandardCharsets.UTF_8),
                suppliedPassword.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Contraseña de anulación incorrecta");
        }
    }
}
