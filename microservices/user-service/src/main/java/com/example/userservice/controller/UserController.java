package com.example.userservice.controller;

import com.example.userservice.dto.*;
import com.example.userservice.model.User;
import com.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Cho phÃƒÂ©p frontend gÃ¡Â»Âi API (CORS)
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/ping")
    public String ping() {
        return "User Service is ALIVE!";
    }

    /**
     * Ã„ÂÃ„â€šNG KÃƒÂ
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "message", "Ã„ÂÃ„Æ’ng kÃƒÂ½ thÃƒÂ nh cÃƒÂ´ng!",
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
     * Ã„ÂÃ„â€šNG NHÃ¡ÂºÂ¬P
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
     * LÃ¡ÂºÂ¤Y TÃ¡ÂºÂ¤T CÃ¡ÂºÂ¢ USERS
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * LÃ¡ÂºÂ¤Y USER THEO ID
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


