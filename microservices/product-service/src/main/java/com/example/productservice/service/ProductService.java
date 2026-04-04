package com.example.productservice.service;

import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy product với id: " + id));
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = getProductById(id);
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setQuantity(updatedProduct.getQuantity());
        existingProduct.setImageUrl(updatedProduct.getImageUrl());
        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    /** Giảm tồn kho (phục vụ Order Service) */
    public void reduceStock(Long id, Integer quantity) {
        Product product = getProductById(id);
        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Sản phẩm '" + product.getName() + "' không đủ hàng tồn kho!");
        }
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
    }

    /** Tăng tồn kho (compensation khi order thất bại) */
    public void increaseStock(Long id, Integer quantity) {
        Product product = getProductById(id);
        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);
    }
}
