package com.yourorg.aicode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserInfo user;
    
    public AuthResponse(String token, String refreshToken, UserInfo user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String imageUrl;
        private String provider;
    }
}
