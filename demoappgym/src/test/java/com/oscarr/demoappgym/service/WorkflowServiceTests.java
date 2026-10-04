package com.oscarr.demoappgym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oscarr.demoappgym.exception.InsufficientStockException;
import com.oscarr.demoappgym.model.dto.AffiliationCoachRequest;
import com.oscarr.demoappgym.model.dto.SaleProductRequest;
import com.oscarr.demoappgym.model.dto.SaleRequest;
import com.oscarr.demoappgym.model.entity.Affiliation;
import com.oscarr.demoappgym.model.entity.Coach;
import com.oscarr.demoappgym.model.entity.Product;
import com.oscarr.demoappgym.model.entity.Sale;
import com.oscarr.demoappgym.model.entity.SaleProduct;
import com.oscarr.demoappgym.model.entity.User;
import com.oscarr.demoappgym.model.entity.TypePlan;
import com.oscarr.demoappgym.repository.AffiliationCoachRepository;
import com.oscarr.demoappgym.repository.AffiliationRepository;
import com.oscarr.demoappgym.repository.CoachRepository;
import com.oscarr.demoappgym.repository.ProductRepository;
import com.oscarr.demoappgym.repository.SaleProductRepository;
import com.oscarr.demoappgym.repository.SaleRepository;
import com.oscarr.demoappgym.repository.TypePlanRepository;
import com.oscarr.demoappgym.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTests {

    @Mock
    private AffiliationRepository affiliationRepository;
    @Mock
    private AffiliationCoachRepository affiliationCoachRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TypePlanRepository typePlanRepository;
    @Mock
    private CoachRepository coachRepository;
    @InjectMocks
    private AffiliationService affiliationService;

    @Mock
    private SaleRepository saleRepository;
    @Mock
    private SaleProductRepository saleProductRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private SaleService saleService;

    @Test
    void createsAffiliationAndPersistsCoachAssignment() {
        User user = new User();
        user.setCodeUser(4L);
        user.setName("Ana");
        TypePlan plan = new TypePlan();
        plan.setIdTypePlan(8L);
        plan.setNamePlan("Mensual");
        Coach coach = new Coach(12L, "Luis");
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);

        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(typePlanRepository.findById(8L)).thenReturn(Optional.of(plan));
        when(coachRepository.findById(12L)).thenReturn(Optional.of(coach));
        when(affiliationRepository.save(any(Affiliation.class))).thenAnswer(invocation -> {
            Affiliation saved = invocation.getArgument(0);
            saved.setNumberAffiliation(25L);
            return saved;
        });

        Affiliation affiliation = new Affiliation();
        affiliation.setUser(user);
        affiliation.setTypePlan(plan);
        affiliation.setAffiliationDate(start);
        affiliation.setEndAffiliation(end);
        AffiliationCoachRequest coachRequest = new AffiliationCoachRequest();
        coachRequest.setCoachId(12L);

        Affiliation saved = affiliationService.saveWithCoaches(affiliation, List.of(coachRequest));

        assertEquals(25L, saved.getNumberAffiliation());
        assertEquals("Ana", saved.getUser().getName());
        assertEquals(8L, saved.getTypePlan().getIdTypePlan());
        assertEquals(start, saved.getAffiliationDate());
        assertEquals(end, saved.getEndAffiliation());
        verify(affiliationCoachRepository).saveAll(anyIterable());
    }

    @Test
    void createsSaleAndReducesProductStock() {
        User user = new User();
        user.setCodeUser(4L);
        user.setName("Ana");
        Product product = new Product(9L, "Agua", 10.0, 5L);
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(product));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale saved = invocation.getArgument(0);
            saved.setIdBillSale(31L);
            return saved;
        });

        SaleRequest request = new SaleRequest(4L, List.of(new SaleProductRequest(9L, 3L)));

        Sale response = saleService.createWithProducts(request);

        assertEquals(31L, response.getIdBillSale());
        assertEquals(30.0, response.getTotalValue());
        assertEquals(2L, product.getAmount());
        var savedSaleProducts = org.mockito.ArgumentCaptor.forClass(Iterable.class);
        verify(saleProductRepository).saveAll(savedSaleProducts.capture());
        SaleProduct saleProduct = (SaleProduct) savedSaleProducts.getValue().iterator().next();
        assertEquals(31L, saleProduct.getId().getIdBillSale());
        assertEquals(9L, saleProduct.getId().getCodeProduct());
        assertEquals(3L, saleProduct.getAmount());
        assertEquals(30.0, saleProduct.getTotalValue());
        verify(productRepository).saveAll(anyIterable());
    }

    @Test
    void rejectsSaleWhenStockIsInsufficient() {
        User user = new User();
        user.setCodeUser(4L);
        Product product = new Product(9L, "Agua", 10.0, 1L);
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(product));

        SaleRequest request = new SaleRequest(4L, List.of(new SaleProductRequest(9L, 2L)));

        assertThrows(InsufficientStockException.class, () -> saleService.createWithProducts(request));
        verify(saleRepository, never()).save(any(Sale.class));
        verify(saleProductRepository, never()).saveAll(anyIterable());
        verify(productRepository, never()).saveAll(anyIterable());
    }
}
