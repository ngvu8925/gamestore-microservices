package com.example.productservice.controller;

import com.example.productservice.model.Product;
import com.example.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .name("Gamepad")
                .description("Gaming Controller")
                .price(50.0)
                .quantity(10)
                .imageUrl("gamepad.png")
                .build();
        testProduct.setId(1L);
    }

    @Test
    @DisplayName("Create Product (201)")
    void createProduct() throws Exception {
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gamepad"));
    }

    @Test
    @DisplayName("Get All Products (200)")
    void getAllProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(testProduct));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Gamepad"));
    }

    @Test
    @DisplayName("Get Product By ID - Thành công (200)")
    void getProductById_Success() throws Exception {
        when(productService.getProductById(1L)).thenReturn(testProduct);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gamepad"));
    }

    @Test
    @DisplayName("Get Product By ID - Thất bại (404)")
    void getProductById_Fail() throws Exception {
        when(productService.getProductById(2L))
                .thenThrow(new RuntimeException("Không tìm thấy product với id: 2"));

        mockMvc.perform(get("/api/products/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Không tìm thấy product với id: 2"));
    }

    @Test
    @DisplayName("Search Products (200)")
    void searchProducts() throws Exception {
        when(productService.searchProducts("game")).thenReturn(List.of(testProduct));

        mockMvc.perform(get("/api/products/search").param("keyword", "game"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Gamepad"));
    }

    @Test
    @DisplayName("Update Product - Thành công (200)")
    void updateProduct_Success() throws Exception {
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gamepad"));
    }

    @Test
    @DisplayName("Update Product - Thất bại (404)")
    void updateProduct_Fail() throws Exception {
        when(productService.updateProduct(eq(2L), any(Product.class)))
                .thenThrow(new RuntimeException("Không tìm thấy product với id: 2"));

        mockMvc.perform(put("/api/products/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Không tìm thấy product với id: 2"));
    }

    @Test
    @DisplayName("Delete Product - Thành công (200)")
    void deleteProduct_Success() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xóa product thành công!"));
    }

    @Test
    @DisplayName("Delete Product - Thất bại (404)")
    void deleteProduct_Fail() throws Exception {
        doThrow(new RuntimeException("Không tìm thấy product với id: 2"))
                .when(productService).deleteProduct(2L);

        mockMvc.perform(delete("/api/products/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Không tìm thấy product với id: 2"));
    }

    @Test
    @DisplayName("Reduce Stock - Thành công (200)")
    void reduceStock_Success() throws Exception {
        doNothing().when(productService).reduceStock(1L, 2);

        mockMvc.perform(put("/api/products/1/reduce-stock").param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật tồn kho thành công!"));
    }

    @Test
    @DisplayName("Reduce Stock - Thất bại (400 - Không đủ kho)")
    void reduceStock_Fail() throws Exception {
        doThrow(new RuntimeException("Sản phẩm 'Gamepad' không đủ hàng tồn kho!"))
                .when(productService).reduceStock(1L, 20);

        mockMvc.perform(put("/api/products/1/reduce-stock").param("quantity", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Sản phẩm 'Gamepad' không đủ hàng tồn kho!"));
    }
}
