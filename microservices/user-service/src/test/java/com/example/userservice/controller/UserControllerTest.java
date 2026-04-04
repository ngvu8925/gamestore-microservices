package com.example.userservice.controller;

import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.LoginResponse;
import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.model.User;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encoded_pass")
                .role("USER")
                .build();
        mockUser.setId(1L);
    }

    @Test
    @DisplayName("Ping endpoint")
    void ping() throws Exception {
        mockMvc.perform(get("/api/users/ping"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Register - Success (201)")
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");

        when(userService.register(any(RegisterRequest.class))).thenReturn(mockUser);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Dang ky thanh cong!"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("Register - Fail (400 - Email exists)")
    void register_Fail() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");

        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Email da duoc su dung!"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email da duoc su dung!"));
    }

    @Test
    @DisplayName("Login - Success (200)")
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        LoginResponse response = LoginResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .accessToken("mock-jwt-token")
                .message("Dang nhap thanh cong!")
                .build();

        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-jwt-token"));
    }

    @Test
    @DisplayName("Login - Fail (401)")
    void login_Fail() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "wrongpass");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Sai mat khau!"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Sai mat khau!"));
    }

    @Test
    @DisplayName("Get all users (200)")
    void getAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(mockUser));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @DisplayName("Get user by id - Success (200)")
    void getUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(mockUser);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("Get user by id - Not found (404)")
    void getUserById_Fail() throws Exception {
        when(userService.getUserById(2L))
                .thenThrow(new RuntimeException("Khong tim thay user voi id: 2"));

        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Khong tim thay user voi id: 2"));
    }
}
