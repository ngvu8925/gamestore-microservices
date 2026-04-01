package com.example.productservice.repository;

import com.example.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // TÃƒÂ¬m product theo tÃƒÂªn (chÃ¡Â»Â©a keyword, khÃƒÂ´ng phÃƒÂ¢n biÃ¡Â»â€¡t hoa thÃ†Â°Ã¡Â»Âng)
    List<Product> findByNameContainingIgnoreCase(String name);
}


