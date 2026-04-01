package com.example.userservice.dto;

public class LoginResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String accessToken;
    private String message;

    public LoginResponse() {}

    public LoginResponse(Long id, String username, String email, String role, String accessToken, String message) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.accessToken = accessToken;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    // MANUAL BUILDER
    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }

    public static class LoginResponseBuilder {
        private Long id;
        private String username;
        private String email;
        private String role;
        private String accessToken;
        private String message;

        public LoginResponseBuilder id(Long id) { this.id = id; return this; }
        public LoginResponseBuilder username(String username) { this.username = username; return this; }
        public LoginResponseBuilder email(String email) { this.email = email; return this; }
        public LoginResponseBuilder role(String role) { this.role = role; return this; }
        public LoginResponseBuilder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public LoginResponseBuilder message(String message) { this.message = message; return this; }

        public LoginResponse build() {
            return new LoginResponse(id, username, email, role, accessToken, message);
        }
    }
}
