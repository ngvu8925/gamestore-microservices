package com.example.gamestore_server.controller;

import com.example.gamestore_server.dto.*;
import com.example.gamestore_server.model.User;
import com.example.gamestore_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Cho phép frontend gọi API (CORS)
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * ĐĂNG KÝ
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "message", "Đăng ký thành công!",
                            "userId", user.getId(),
                            "username", user.getUsername()
                    )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * ĐĂNG NHẬP
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * LẤY TẤT CẢ USERS
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * LẤY USER THEO ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", e.getMessage())
            );
        }
    }
}
