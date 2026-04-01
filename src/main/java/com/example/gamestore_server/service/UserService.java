package com.example.gamestore_server.service;

import com.example.gamestore_server.dto.*;
import com.example.gamestore_server.model.User;
import com.example.gamestore_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Dùng BCrypt để mã hóa password (bảo mật, không lưu password thô)
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * ĐĂNG KÝ - Tạo tài khoản mới
     */
    public User register(RegisterRequest request) {
        // 1. Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        // 2. Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã được sử dụng!");
        }

        // 3. Tạo user mới, mã hóa password trước khi lưu
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // 4. Lưu vào database
        return userRepository.save(user);
    }

    /**
     * ĐĂNG NHẬP - Xác thực tài khoản
     */
    public LoginResponse login(LoginRequest request) {
        // 1. Tìm user theo email
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Email không tồn tại!");
        }

        User user = optionalUser.get();

        // 2. So sánh password (password đã mã hóa trong DB)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Sai mật khẩu!");
        }

        // 3. Trả về thông tin user (KHÔNG có password)
        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .message("Đăng nhập thành công!")
                .build();
    }

    /**
     * Lấy tất cả users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Lấy user theo ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));
    }
}
