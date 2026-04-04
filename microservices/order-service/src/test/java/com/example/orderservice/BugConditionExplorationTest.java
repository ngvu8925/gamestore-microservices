package com.example.orderservice;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.client.UserClient;
import com.example.orderservice.controller.OrderController;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.ProductDTO;
import com.example.orderservice.exception.GlobalExceptionHandler;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Bug Condition Exploration Tests
 * These tests verify the bugs are FIXED (pass after fix).
 * Validates: Requirements 1.1, 1.2, 1.3, 2.1, 3.1
 */
public class BugConditionExplorationTest {

    // -------------------------------------------------------------------------
    // Test 1.1 - Dockerfile Parse Test
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("isBugCondition_Dockerfile - ENTRYPOINT must use JSON array syntax in all 5 Dockerfiles")
    void isBugCondition_Dockerfile() throws IOException {
        Pattern jsonArrayPattern = Pattern.compile(
                "ENTRYPOINT \\[\"[^\"]+\", \"[^\"]+\"(, \"[^\"]+\")*\\]"
        );

        Path moduleRoot = Paths.get("").toAbsolutePath();
        Path microservicesRoot = moduleRoot.getParent();

        String[] serviceNames = {"discovery-server", "gateway-service", "user-service", "product-service", "order-service"};

        for (String service : serviceNames) {
            Path dockerfilePath = microservicesRoot.resolve(service).resolve("Dockerfile");
            assertTrue(Files.exists(dockerfilePath), "Dockerfile not found: " + dockerfilePath);

            String content = Files.readString(dockerfilePath);
            assertTrue(
                    jsonArrayPattern.matcher(content).find(),
                    "COUNTEREXAMPLE - Dockerfile ENTRYPOINT does not use JSON array syntax in: "
                            + service + "\nActual content:\n" + content
            );
        }
    }

    // -------------------------------------------------------------------------
    // Test 1.2 - Gateway Config Test
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("isBugCondition_GatewayRoutes - Gateway URIs must not contain localhost")
    void isBugCondition_GatewayRoutes() throws IOException {
        Path moduleRoot = Paths.get("").toAbsolutePath();
        Path microservicesRoot = moduleRoot.getParent();
        Path configPath = microservicesRoot.resolve("gateway-service/src/main/resources/application.yml");

        assertTrue(Files.exists(configPath), "application.yml not found: " + configPath);

        String content = Files.readString(configPath);
        assertFalse(
                content.contains("localhost"),
                "COUNTEREXAMPLE - Gateway application.yml contains 'localhost' URI.\n" + content
        );
    }

    // -------------------------------------------------------------------------
    // Test 1.3 - Hardcoded Secrets Test
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("isBugCondition_HardcodedSecrets - No plaintext passwords or JWT secrets in source files")
    void isBugCondition_HardcodedSecrets() throws IOException {
        Path moduleRoot = Paths.get("").toAbsolutePath();
        Path microservicesRoot = moduleRoot.getParent();

        // Pattern: password=<value> that is NOT a placeholder ${...}
        Pattern hardcodedPasswordPattern = Pattern.compile("password=(?!\\$\\{)[^\\s]+");

        String[] propFiles = {
                "order-service/src/main/resources/application.properties",
                "user-service/src/main/resources/application.properties",
                "product-service/src/main/resources/application.properties"
        };

        for (String rel : propFiles) {
            Path path = microservicesRoot.resolve(rel);
            if (!Files.exists(path)) continue;
            String content = Files.readString(path);
            assertFalse(
                    hardcodedPasswordPattern.matcher(content).find(),
                    "COUNTEREXAMPLE - Hardcoded password found in: " + rel
            );
        }

        // Check AuthenticationFilter.java for hardcoded JWT secret
        Path filterPath = microservicesRoot.resolve(
                "gateway-service/src/main/java/com/example/gatewayservice/filter/AuthenticationFilter.java"
        );
        assertTrue(Files.exists(filterPath), "AuthenticationFilter.java not found: " + filterPath);

        String filterContent = Files.readString(filterPath);
        Pattern hardcodedSecretPattern = Pattern.compile("SECRET\\s*=\\s*\"[^\"]+\"");
        assertFalse(
                hardcodedSecretPattern.matcher(filterContent).find(),
                "COUNTEREXAMPLE - Hardcoded JWT secret found in AuthenticationFilter.java."
        );
    }

    // -------------------------------------------------------------------------
    // Test 1.4 - Order Partial Failure (mock-based, no Docker needed)
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("isBugCondition_OrderTransaction - Stock compensation called when partial reduceStock fails")
    void isBugCondition_OrderTransaction() {
        // Use mocks directly without Spring context
        ProductClient productClient = mock(ProductClient.class);
        UserClient userClient = mock(UserClient.class);
        com.example.orderservice.repository.OrderRepository orderRepository =
                mock(com.example.orderservice.repository.OrderRepository.class);

        OrderService orderService = new OrderService();
        // Inject mocks via reflection
        injectField(orderService, "productClient", productClient);
        injectField(orderService, "userClient", userClient);
        injectField(orderService, "orderRepository", orderRepository);

        Long userId = 1L;
        Long productId0 = 10L;
        Long productId1 = 20L;

        OrderItem item0 = new OrderItem(productId0, 1, 0.0);
        OrderItem item1 = new OrderItem(productId1, 2, 0.0);

        ProductDTO product0 = new ProductDTO();
        product0.setId(productId0);
        product0.setPrice(100.0);

        ProductDTO product1 = new ProductDTO();
        product1.setId(productId1);
        product1.setPrice(200.0);

        doNothing().when(userClient).getUserById(userId);
        when(productClient.getProductById(productId0)).thenReturn(product0);
        when(productClient.getProductById(productId1)).thenReturn(product1);
        doNothing().when(productClient).reduceStock(eq(productId0), anyInt());
        doThrow(new RuntimeException("Insufficient stock for product " + productId1))
                .when(productClient).reduceStock(eq(productId1), anyInt());

        assertThrows(Exception.class, () ->
                orderService.placeOrder(userId, List.of(item0, item1))
        );

        // After fix: increaseStock must be called for item[0] as compensation
        verify(productClient, times(1)).increaseStock(eq(productId0), eq(1));
    }

    private void injectField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }

    // -------------------------------------------------------------------------
    // Test 1.5 - Bean Validation Test
    // -------------------------------------------------------------------------
    @WebMvcTest(OrderController.class)
    @Import(GlobalExceptionHandler.class)
    @ActiveProfiles("test")
    static class BeanValidationTest {

        @Autowired
        MockMvc mockMvc;

        @Autowired
        ObjectMapper objectMapper;

        @MockBean
        OrderService orderService;

        @Test
        @DisplayName("isBugCondition_MissingValidation - OrderRequest with userId=null must return HTTP 400")
        void isBugCondition_MissingValidation() throws Exception {
            OrderRequest request = new OrderRequest();
            request.setUserId(null);
            OrderRequest.OrderItemEntry item = new OrderRequest.OrderItemEntry();
            item.setProductId(1L);
            item.setQuantity(1);
            request.setItems(List.of(item));

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
