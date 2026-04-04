package com.example.orderservice;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.client.UserClient;
import com.example.orderservice.dto.ProductDTO;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Preservation Property Tests
 *
 * These tests MUST PASS on UNFIXED code (baseline behavior).
 * They verify that valid order placement behavior is preserved after fixes.
 *
 * Property: For all valid OrderRequest (userId != null, items non-empty, all services up),
 * totalAmount equals sum of (price * quantity) per item.
 *
 * Validates: Requirements 3.1, 3.2, 3.3
 */
@SpringBootTest
@Testcontainers
class PreservationPropertyTest {

    @Container
    static final MSSQLServerContainer<?> SQLSERVER_CONTAINER =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
                    .acceptLicense();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SQLSERVER_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", SQLSERVER_CONTAINER::getUsername);
        registry.add("spring.datasource.password", SQLSERVER_CONTAINER::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("spring.cloud.discovery.enabled", () -> "false");
    }

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @MockBean
    UserClient userClient;

    @MockBean
    ProductClient productClient;

    private static final Random RANDOM = new Random(42);

    /**
     * Property: totalAmount = sum(price * quantity) for a single item order.
     * MUST PASS on unfixed code.
     */
    @Test
    @DisplayName("Preservation - Single item: totalAmount equals price * quantity")
    void preservation_singleItem_totalAmountCorrect() {
        Long userId = 1L;
        Long productId = 10L;
        double price = 99.99;
        int quantity = 3;

        ProductDTO product = new ProductDTO();
        product.setId(productId);
        product.setPrice(price);

        doNothing().when(userClient).getUserById(userId);
        when(productClient.getProductById(productId)).thenReturn(product);
        doNothing().when(productClient).reduceStock(anyLong(), anyInt());

        OrderItem item = new OrderItem();
        item.setProductId(productId);
        item.setQuantity(quantity);

        Order result = orderService.placeOrder(userId, List.of(item));

        assertNotNull(result.getId(), "Order must be persisted with an ID");
        assertEquals(userId, result.getUserId());
        assertEquals(price * quantity, result.getTotalAmount(), 0.001,
                "totalAmount must equal price * quantity");
    }

    /**
     * Property-based: For random valid inputs (1-5 items, price 1-500, qty 1-10),
     * totalAmount always equals sum(price * quantity).
     * MUST PASS on unfixed code.
     */
    @RepeatedTest(10)
    @DisplayName("Preservation PBT - Random valid order: totalAmount = sum(price * quantity)")
    void preservation_randomValidOrder_totalAmountCorrect() {
        Long userId = (long) (RANDOM.nextInt(100) + 1);
        int itemCount = RANDOM.nextInt(5) + 1;

        List<OrderItem> items = new ArrayList<>();
        double expectedTotal = 0.0;

        doNothing().when(userClient).getUserById(anyLong());

        for (int i = 0; i < itemCount; i++) {
            Long productId = (long) (i + 1);
            double price = Math.round((RANDOM.nextDouble() * 499 + 1) * 100.0) / 100.0;
            int quantity = RANDOM.nextInt(10) + 1;

            ProductDTO product = new ProductDTO();
            product.setId(productId);
            product.setPrice(price);

            when(productClient.getProductById(productId)).thenReturn(product);
            doNothing().when(productClient).reduceStock(anyLong(), anyInt());

            OrderItem item = new OrderItem();
            item.setProductId(productId);
            item.setQuantity(quantity);
            items.add(item);

            expectedTotal += price * quantity;
        }

        Order result = orderService.placeOrder(userId, items);

        assertNotNull(result.getId(), "Order must be persisted");
        assertEquals(expectedTotal, result.getTotalAmount(), 0.01,
                "totalAmount must equal sum(price * quantity) for all items");
        assertEquals(itemCount, result.getItems().size(),
                "Order must contain all items");
    }

    /**
     * Property: Order is persisted in DB after successful placement.
     * MUST PASS on unfixed code.
     */
    @Test
    @DisplayName("Preservation - Successful order is persisted in database")
    void preservation_successfulOrder_persistedInDB() {
        Long userId = 42L;
        Long productId = 5L;

        ProductDTO product = new ProductDTO();
        product.setId(productId);
        product.setPrice(50.0);

        doNothing().when(userClient).getUserById(userId);
        when(productClient.getProductById(productId)).thenReturn(product);
        doNothing().when(productClient).reduceStock(anyLong(), anyInt());

        OrderItem item = new OrderItem();
        item.setProductId(productId);
        item.setQuantity(2);

        Order result = orderService.placeOrder(userId, List.of(item));

        assertTrue(orderRepository.findById(result.getId()).isPresent(),
                "Order must be findable in DB after placement");
        assertEquals("PENDING", result.getStatus(),
                "New order status must be PENDING");
    }
}
