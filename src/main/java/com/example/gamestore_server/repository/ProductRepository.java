package com.example.gamestore_server.repository;

import com.example.gamestore_server.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Tìm product theo tên (chứa keyword, không phân biệt hoa thường)
    List<Product> findByNameContainingIgnoreCase(String name);
}
