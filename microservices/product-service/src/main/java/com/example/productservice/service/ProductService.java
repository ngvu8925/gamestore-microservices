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

    /**
     * TÃ¡ÂºÂ O product mÃ¡Â»â€ºi
     */
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    /**
     * LÃ¡ÂºÂ¤Y tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * LÃ¡ÂºÂ¤Y product theo ID
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y product vÃ¡Â»â€ºi id: " + id));
    }

    /**
     * TÃƒÅ’M KIÃ¡ÂºÂ¾M product theo tÃƒÂªn
     */
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * CÃ¡ÂºÂ¬P NHÃ¡ÂºÂ¬T product
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
     * XÃƒâ€œA product
     */
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    /**
     * GIÃ¡ÂºÂ¦M TÃ¡Â»Â’N KHO (PhÃ¡Â»Â¥c vÃ¡Â»Â¥ Order Service)
     */
    public void reduceStock(Long id, Integer quantity) {
        Product product = getProductById(id);
        if (product.getQuantity() < quantity) {
            throw new RuntimeException("SÃ¡ÂºÂ£n phÃ¡ÂºÂ©m '" + product.getName() + "' khÃƒÂ´ng Ã„â€˜Ã¡Â»Â§ hàng tồn kho!");
        }
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
    }
}


