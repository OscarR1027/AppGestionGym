package com.oscarr.demoappgym.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oscarr.demoappgym.exception.BusinessException;
import com.oscarr.demoappgym.exception.InsufficientStockException;
import com.oscarr.demoappgym.exception.ResourceNotFoundException;
import com.oscarr.demoappgym.model.dto.MonthlyRevenueResponse;
import com.oscarr.demoappgym.model.dto.SaleProductRequest;
import com.oscarr.demoappgym.model.dto.SaleRequest;
import com.oscarr.demoappgym.model.entity.Product;
import com.oscarr.demoappgym.model.entity.Sale;
import com.oscarr.demoappgym.model.entity.SaleProduct;
import com.oscarr.demoappgym.model.entity.SaleProductId;
import com.oscarr.demoappgym.model.entity.User;
import com.oscarr.demoappgym.repository.ProductRepository;
import com.oscarr.demoappgym.repository.SaleProductRepository;
import com.oscarr.demoappgym.repository.SaleRepository;
import com.oscarr.demoappgym.repository.UserRepository;

@Service 
public class SaleService implements GenericCrud<Sale,Long> {
    
    private final SaleRepository saleRepository;
    private final SaleProductRepository saleProductRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public SaleService(
            SaleRepository saleRepository,
            SaleProductRepository saleProductRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.saleRepository=saleRepository;
        this.saleProductRepository = saleProductRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
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
        Optional<Sale> sale = saleRepository.findById(id);
        if (sale.isEmpty()) {
            return;
        }
        List<SaleProduct> saleProducts = saleProductRepository.findAllBySaleId(id);
        Map<Long, Product> productsToRestore = new HashMap<>();
        for (Long productId : saleProducts.stream()
                .map(saleProduct -> saleProduct.getProduct().getCode_Products())
                .distinct()
                .sorted()
                .toList()) {
            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró el producto con ID " + productId + " para restaurar el inventario"));
            productsToRestore.put(productId, product);
        }
        for (SaleProduct saleProduct : saleProducts) {
            Product product = productsToRestore.get(saleProduct.getProduct().getCode_Products());
            if (product.getAmount() == null) {
                throw new BusinessException(
                        "No se puede restaurar el inventario del producto " + product.getName_Product());
            }
            product.setAmount(product.getAmount() + saleProduct.getAmount());
        }
        productRepository.saveAll(productsToRestore.values());
        saleProductRepository.deleteAll(saleProducts);
        saleRepository.delete(sale.get());
    }

    @Transactional
    public Sale createWithProducts(SaleRequest request) {
        return saveSaleWithProducts(new Sale(), request);
    }

    @Transactional
    public Sale updateWithProducts(Long saleId, SaleRequest request) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la venta con ID " + saleId));
        List<SaleProduct> previousProducts = saleProductRepository.findAllBySaleId(saleId);
        Map<Long, Product> productsToRestore = new HashMap<>();
        for (Long productId : previousProducts.stream()
                .map(saleProduct -> saleProduct.getProduct().getCode_Products())
                .distinct()
                .sorted()
                .toList()) {
            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró el producto con ID " + productId + " para restaurar el inventario"));
            productsToRestore.put(productId, product);
        }
        for (SaleProduct previousProduct : previousProducts) {
            Product product = productsToRestore.get(previousProduct.getProduct().getCode_Products());
            if (product.getAmount() == null) {
                throw new BusinessException(
                        "No se puede restaurar el inventario del producto " + product.getName_Product());
            }
            product.setAmount(product.getAmount() + previousProduct.getAmount());
        }
        productRepository.saveAll(productsToRestore.values());
        saleProductRepository.deleteAllInBatch(previousProducts);
        saleProductRepository.flush();
        return saveSaleWithProducts(sale, request);
    }

    private Sale saveSaleWithProducts(Sale sale, SaleRequest request) {
        if (request == null || request.getUserId() == null
                || request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new BusinessException("El usuario y al menos un producto son obligatorios");
        }
        if (!isSupportedPaymentMethod(request.getPaymentMethod())) {
            throw new BusinessException("Selecciona efectivo, tarjeta o transferencia como método de pago");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario con ID " + request.getUserId()));
        HashSet<Long> productIds = new HashSet<>();
        for (SaleProductRequest productRequest : request.getProducts()) {
            if (productRequest == null) {
                throw new BusinessException("La lista de productos no puede contener valores nulos");
            }
            if (productRequest.getProductId() == null || productRequest.getAmount() == null
                    || productRequest.getAmount() <= 0) {
                throw new BusinessException("Cada producto debe tener un ID y una cantidad mayor que cero");
            }
            if (!productIds.add(productRequest.getProductId())) {
                throw new BusinessException(
                        "El producto con ID " + productRequest.getProductId() + " está repetido en la venta");
            }
        }

        Map<Long, Product> productsById = new HashMap<>();
        for (Long productId : productIds.stream().sorted().toList()) {
            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró el producto con ID " + productId));
            productsById.put(productId, product);
        }

        BigDecimal total = BigDecimal.ZERO;
        List<SaleProduct> saleProducts = new ArrayList<>();
        for (SaleProductRequest productRequest : request.getProducts()) {
            Product product = productsById.get(productRequest.getProductId());
            Long availableAmount = product.getAmount();
            if (availableAmount == null || availableAmount < productRequest.getAmount()) {
                throw new InsufficientStockException(
                        "Stock insuficiente para el producto " + product.getName_Product());
            }
            if (product.getValue() == null || product.getValue() < 0) {
                throw new BusinessException(
                        "El precio del producto " + product.getName_Product() + " no es válido");
            }

            BigDecimal subtotal = BigDecimal.valueOf(product.getValue())
                    .multiply(BigDecimal.valueOf(productRequest.getAmount()));
            total = total.add(subtotal);
            product.setAmount(availableAmount - productRequest.getAmount());
            saleProducts.add(new SaleProduct(
                    null,
                    product,
                    null,
                    productRequest.getAmount(),
                    subtotal.doubleValue()));
        }

        sale.setUser(user);
        sale.setSaleDate(LocalDate.now());
        sale.setTotalValue(total.doubleValue());
        sale.setPaymentMethod(request.getPaymentMethod());
        sale = saleRepository.save(sale);

        for (SaleProduct saleProduct : saleProducts) {
            saleProduct.setSale(sale);
            saleProduct.setId(new SaleProductId(
                    sale.getIdBillSale(),
                    saleProduct.getProduct().getCode_Products()));
        }
        saleProductRepository.saveAll(saleProducts);
        productRepository.saveAll(productsById.values());
        return sale;
    }

    private boolean isSupportedPaymentMethod(String paymentMethod) {
        return "CASH".equals(paymentMethod) || "CARD".equals(paymentMethod) || "TRANSFER".equals(paymentMethod);
    }

    @Transactional(readOnly = true)
    public List<SaleProduct> findProductsBySaleId(Long saleId) {
        return saleProductRepository.findAllBySaleId(saleId);
    }

    @Transactional(readOnly = true)
    public MonthlyRevenueResponse getCurrentMonthRevenue() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.plusMonths(1).atDay(1);
        return new MonthlyRevenueResponse(saleRepository.sumTotalValueBetweenDates(startDate, endDate));
    }
}
