package com.example.orderservice.client.fallback;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.dto.ProductDTO;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductDTO getProductById(Long id) {
        throw new RuntimeException("Product service tạm thời không khả dụng. Vui lòng thử lại sau.");
    }

    @Override
    public void reduceStock(Long id, Integer quantity) {
        throw new RuntimeException("Product service tạm thời không khả dụng. Không thể trừ tồn kho.");
    }

    @Override
    public void increaseStock(Long id, Integer quantity) {
        throw new RuntimeException("Product service tạm thời không khả dụng. Không thể hoàn lại tồn kho.");
    }
}
