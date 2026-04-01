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

    // DÃƒÂ¹ng BCrypt Ã„â€˜Ã¡Â»Æ’ mÃƒÂ£ hÃƒÂ³a password (bÃ¡ÂºÂ£o mÃ¡ÂºÂ­t, khÃƒÂ´ng lÃ†Â°u password thÃƒÂ´)
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Ã„Â Ã„â€šNG KÃƒÂ  - TÃ¡ÂºÂ¡o tÃƒÂ i khoÃ¡ÂºÂ£n mÃ¡Â»â€ºi
     */
    public User register(RegisterRequest request) {
        // 1. KiÃ¡Â»Æ’m tra email Ã„â€˜ÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i chÃ†Â°a
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c sÃ¡Â»Â­ dÃ¡Â»Â¥ng!");
        }

        // 2. KiÃ¡Â»Æ’m tra username Ã„â€˜ÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i chÃ†Â°a
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c sÃ¡Â»Â­ dÃ¡Â»Â¥ng!");
        }

        // 3. TÃ¡ÂºÂ¡o user mÃ¡Â»â€ºi, mÃƒÂ£ hÃƒÂ³a password trÃ†Â°Ã¡Â»â€ºc khi lÃ†Â°u
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // 4. LÃ†Â°u vÃƒÂ o database
        return userRepository.save(user);
    }

    /**
     * Ã„ÂÃ„â€šNG NHÃ¡ÂºÂ¬P - XÃƒÂ¡c thÃ¡Â»Â±c tÃƒÂ i khoÃ¡ÂºÂ£n
     */
    public LoginResponse login(LoginRequest request) {
        // 1. TÃƒÂ¬m user theo email
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Email khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i!");
        }

        User user = optionalUser.get();

        // 2. So sÃƒÂ¡nh password (password Ã„â€˜ÃƒÂ£ mÃƒÂ£ hÃƒÂ³a trong DB)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Sai mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u!");
        }

        // 5. TÃ¡ÂºÂ¡o JWT Token
        String token = jwtUtils.generateToken(user.getUsername(), user.getId());

        // 6. TrÃ¡ÂºÂ£ vÃ¡Â»Â  phÃ¡ÂºÂ£n hÃ¡Â»â€œi dÃ†Â°Ã¡Â»Âºi dÃ¡ÂºÂ¡ng DTO
        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(token)
                .message("Ã„Â Ã„Æ’ng nhÃ¡ÂºÂ­p thÃƒÂ nh cÃƒÂ´ng!")
                .build();
    }

    /**
     * LÃ¡ÂºÂ¥y tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * LÃ¡ÂºÂ¥y user theo ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y user vÃ¡Â»â€ºi id: " + id));
    }
}


