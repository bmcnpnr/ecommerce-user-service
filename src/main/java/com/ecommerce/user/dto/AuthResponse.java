package com.ecommerce.user.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private long expiresIn;
    private String username;
    private String role;
}
