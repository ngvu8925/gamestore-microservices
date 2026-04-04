package com.example.userservice.service;

import com.example.userservice.dto.*;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    // Dung BCrypt de ma hoa password
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * DANG KY - Tao tai khoan moi
     */
    public User register(RegisterRequest request) {
        // 1. Kiem tra email da ton tai chua
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email da duoc su dung!");
        }

        // 2. Kiem tra username da ton tai chua
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username da duoc su dung!");
        }

        // 3. Tao user moi, ma hoa password truoc khi luu
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // 4. Luu vao database
        return userRepository.save(user);
    }

    /**
     * DANG NHAP - Xac thuc tai khoan
     */
    public LoginResponse login(LoginRequest request) {
        // 1. Tim user theo email
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Email khong ton tai!");
        }

        User user = optionalUser.get();

        // 2. So sanh password (password da ma hoa trong DB)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Sai mat khau!");
        }

        // 3. Tao JWT Token
        String token = jwtUtils.generateToken(user.getUsername(), user.getId());

        // 4. Tra ve phan hoi dang DTO
        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(token)
                .message("Dang nhap thanh cong!")
                .build();
    }

    /**
     * Lay tat ca users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Lay user theo ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay user voi id: " + id));
    }
}
