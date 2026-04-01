package com.example.gamestore_server.repository;

import com.example.gamestore_server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm user bằng email (dùng cho đăng nhập)
    Optional<User> findByEmail(String email);

    // Kiểm tra email đã tồn tại chưa (dùng cho đăng ký)
    boolean existsByEmail(String email);

    // Kiểm tra username đã tồn tại chưa
    boolean existsByUsername(String username);
}
