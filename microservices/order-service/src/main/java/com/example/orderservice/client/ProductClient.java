package com.example.orderservice.client;

import com.example.orderservice.client.fallback.ProductClientFallback;
import com.example.orderservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {
    
    @GetMapping("/api/products/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);

    @PutMapping("/api/products/{id}/reduce-stock")
    void reduceStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);

    @PutMapping("/api/products/{id}/increase-stock")
    void increaseStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);
}
