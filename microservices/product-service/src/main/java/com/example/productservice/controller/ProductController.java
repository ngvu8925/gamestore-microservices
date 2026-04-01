package com.example.productservice.controller;

import com.example.productservice.model.Product;
import com.example.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * TÃ¡ÂºÂ O PRODUCT MÃ¡Â»Å¡I
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * LÃ¡ÂºÂ¤Y TÃ¡ÂºÂ¤T CÃ¡ÂºÂ¢ PRODUCTS
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * LÃ¡ÂºÂ¤Y PRODUCT THEO ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * TÃƒÅ’M KIÃ¡ÂºÂ¾M PRODUCTS THEO TÃƒÅ N
     * GET /api/products/search?keyword=gta
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    /**
     * CÃ¡ÂºÂ¬P NHÃ¡ÂºÂ¬T PRODUCT
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            Product updated = productService.updateProduct(id, product);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * XÃƒâ€œA PRODUCT
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(
                    Map.of("message", "XÃƒÂ³a product thÃƒÂ nh cÃƒÂ´ng!", "id", id)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * GIÃ¡ÂºÂ¦M TÃ¡Â»Â’N KHO
     * PUT /api/products/{id}/reduce-stock?quantity=2
     */
    @PutMapping("/{id}/reduce-stock")
    public ResponseEntity<?> reduceStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            productService.reduceStock(id, quantity);
            return ResponseEntity.ok(
                    Map.of("message", "CÃ¡ÂºÂ-p nhÃ¡ÂºÂ-t tÃ¡Â»â€œn kho thÃƒÂ nh cÃƒÂ´ng!", "id", id)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
}


