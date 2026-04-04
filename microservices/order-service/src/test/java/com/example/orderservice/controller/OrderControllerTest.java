package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.exception.GlobalExceptionHandler;
import com.example.orderservice.model.Order;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    OrderService orderService;

    @Test
    @DisplayName("POST /api/orders - valid request returns 201")
    void placeOrder_ValidRequest_Returns201() throws Exception {
        Order mockOrder = new Order(1L, 300.0);
        mockOrder.setId(1L);

        when(orderService.placeOrder(anyLong(), any())).thenReturn(mockOrder);

        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        OrderRequest.OrderItemEntry item = new OrderRequest.OrderItemEntry();
        item.setProductId(10L);
        item.setQuantity(2);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/orders - userId null returns 400")
    void placeOrder_NullUserId_Returns400() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(null);
        OrderRequest.OrderItemEntry item = new OrderRequest.OrderItemEntry();
        item.setProductId(10L);
        item.setQuantity(1);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/orders - empty items returns 400")
    void placeOrder_EmptyItems_Returns400() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setItems(List.of());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/orders - quantity 0 returns 400")
    void placeOrder_ZeroQuantity_Returns400() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        OrderRequest.OrderItemEntry item = new OrderRequest.OrderItemEntry();
        item.setProductId(10L);
        item.setQuantity(0);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/orders - service throws RuntimeException returns 400")
    void placeOrder_ServiceThrows_Returns400() throws Exception {
        when(orderService.placeOrder(anyLong(), any()))
                .thenThrow(new RuntimeException("User không tồn tại!"));

        OrderRequest request = new OrderRequest();
        request.setUserId(99L);
        OrderRequest.OrderItemEntry item = new OrderRequest.OrderItemEntry();
        item.setProductId(10L);
        item.setQuantity(1);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User không tồn tại!"));
    }
}
