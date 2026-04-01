package com.example.gamestore_server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String message;
}
