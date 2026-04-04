package com.example.orderservice.service;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.client.UserClient;
import com.example.orderservice.dto.ProductDTO;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class OrderServiceIntegrationTest {

    @Container
    private static final MSSQLServerContainer<?> SQLSERVER_CONTAINER = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SQLSERVER_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", SQLSERVER_CONTAINER::getUsername);
        registry.add("spring.datasource.password", SQLSERVER_CONTAINER::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("eureka.client.enabled", () -> "false"); // Tắt Eureka client lúc test
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private UserClient userClient;

    @MockBean
    private ProductClient productClient;

    @Test
    @DisplayName("Đặt hàng (Place Order) - Integration Flow với Testcontainers")
    void placeOrder_IntegrationFlow() {
        // 1. Mock Data
        Long userId = 100L;
        Long productId = 50L;

        OrderItem item = new OrderItem();
        item.setProductId(productId);
        item.setQuantity(2);

        ProductDTO mockProduct = new ProductDTO();
        mockProduct.setId(productId);
        mockProduct.setPrice(150.0);

        // 2. Mock Feign Clients
        when(userClient.getUserById(userId)).thenReturn(new Object());
        when(productClient.getProductById(productId)).thenReturn(mockProduct);
        doNothing().when(productClient).reduceStock(eq(productId), eq(2));

        // 3. Thực thi
        Order savedOrder = orderService.placeOrder(userId, List.of(item));

        // 4. Assertions
        assertNotNull(savedOrder);
        assertNotNull(savedOrder.getId());
        assertEquals(userId, savedOrder.getUserId());
        assertEquals(300.0, savedOrder.getTotalAmount()); // 150.0 * 2
        assertEquals(1, savedOrder.getItems().size());
        
        // Items must also be saved
        OrderItem savedItem = savedOrder.getItems().get(0);
        assertEquals(productId, savedItem.getProductId());
        assertEquals(150.0, savedItem.getPrice());
        assertEquals(savedOrder, savedItem.getOrder());

        // 5. Verify Feign interaction
        verify(userClient, times(1)).getUserById(userId);
        verify(productClient, times(1)).getProductById(productId);
        verify(productClient, times(1)).reduceStock(productId, 2);
    }
}
