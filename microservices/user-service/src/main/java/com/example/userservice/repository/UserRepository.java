package com.example.userservice.repository;

import com.example.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // TÃƒÂ¬m user bÃ¡ÂºÂ±ng email (dÃƒÂ¹ng cho Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p)
    Optional<User> findByEmail(String email);

    // KiÃ¡Â»Æ’m tra email Ã„â€˜ÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i chÃ†Â°a (dÃƒÂ¹ng cho Ã„â€˜Ã„Æ’ng kÃƒÂ½)
    boolean existsByEmail(String email);

    // KiÃ¡Â»Æ’m tra username Ã„â€˜ÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i chÃ†Â°a
    boolean existsByUsername(String username);
}


