package com.example.productservice.service;

import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

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
    @DisplayName("Tạo mới Product")
    void createProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.createProduct(testProduct);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Gamepad", result.getName());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    @DisplayName("Lấy tất cả Products")
    void getAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(testProduct));

        List<Product> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Gamepad", result.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Lấy Product theo ID thành công")
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Gamepad", result.getName());
    }

    @Test
    @DisplayName("Lấy Product theo ID thất bại - Không tìm thấy")
    void getProductById_Fail_NotFound() {
        when(productRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.getProductById(2L));
        assertEquals("Không tìm thấy product với id: 2", exception.getMessage());
    }

    @Test
    @DisplayName("Tìm kiếm Product theo tên")
    void searchProducts() {
        when(productRepository.findByNameContainingIgnoreCase("game")).thenReturn(List.of(testProduct));

        List<Product> result = productService.searchProducts("game");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Gamepad", result.get(0).getName());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("game");
    }

    @Test
    @DisplayName("Cập nhật Product thành công")
    void updateProduct() {
        Product updatedData = Product.builder()
                .name("Gamepad Pro")
                .description("Pro Gaming Controller")
                .price(60.0)
                .quantity(5)
                .imageUrl("gamepad_pro.png")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.updateProduct(1L, updatedData);

        assertNotNull(result);
        assertEquals("Gamepad Pro", result.getName());
        assertEquals(60.0, result.getPrice());
        assertEquals(5, result.getQuantity());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Xóa Product thành công")
    void deleteProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(testProduct);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));

        verify(productRepository, times(1)).delete(testProduct);
    }

    @Test
    @DisplayName("Giảm tồn kho thành công")
    void reduceStock_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.reduceStock(1L, 3);

        assertEquals(7, testProduct.getQuantity());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    @DisplayName("Giảm tồn kho thất bại - Không đủ số lượng")
    void reduceStock_Fail_NotEnoughStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.reduceStock(1L, 15));
        assertEquals("Sản phẩm 'Gamepad' không đủ hàng tồn kho!", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }
}
