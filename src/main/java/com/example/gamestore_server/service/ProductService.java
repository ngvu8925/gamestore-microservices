package com.example.gamestore_server.service;

import com.example.gamestore_server.model.Product;
import com.example.gamestore_server.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * TẠO product mới
     */
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    /**
     * LẤY tất cả products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * LẤY product theo ID
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy product với id: " + id));
    }

    /**
     * TÌM KIẾM product theo tên
     */
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * CẬP NHẬT product
     */
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = getProductById(id);

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setQuantity(updatedProduct.getQuantity());
        existingProduct.setImageUrl(updatedProduct.getImageUrl());

        return productRepository.save(existingProduct);
    }

    /**
     * XÓA product
     */
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}
