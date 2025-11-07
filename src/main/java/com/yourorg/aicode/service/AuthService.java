package com.yourorg.aicode.service;

import com.yourorg.aicode.dto.AuthResponse;
import com.yourorg.aicode.dto.LoginRequest;
import com.yourorg.aicode.dto.RegisterRequest;
import com.yourorg.aicode.model.RefreshToken;
import com.yourorg.aicode.model.User;
import com.yourorg.aicode.repository.UserRepository;
import com.yourorg.aicode.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest, HttpServletRequest request) {
        // Validate passwords match
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        // Create new user
        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setProvider(User.AuthProvider.LOCAL);
        user.setEmailVerified(false);
        
        user = userRepository.save(user);
        
        // Generate JWT token
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                registerRequest.getEmail(),
                registerRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);
        
        // Create refresh token
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
            user.getId(), ipAddress, userAgent
        );
        
        // Build response
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getImageUrl(),
            user.getProvider().name()
        );
        
        return new AuthResponse(token, refreshToken.getToken(), userInfo);
    }
    
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);
        
        // Get user info
        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create refresh token
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
            user.getId(), ipAddress, userAgent
        );
        
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getImageUrl(),
            user.getProvider().name()
        );
        
        return new AuthResponse(token, refreshToken.getToken(), userInfo);
    }
    
    public AuthResponse refreshToken(String refreshTokenStr, HttpServletRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr);
        refreshTokenService.verifyExpiration(refreshToken);
        
        User user = refreshToken.getUser();
        String newAccessToken = tokenProvider.generateTokenFromEmail(user.getEmail());
        
        // Optionally rotate refresh token for better security
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(
            user.getId(), ipAddress, userAgent
        );
        
        // Revoke old refresh token
        refreshTokenService.revokeToken(refreshTokenStr);
        
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getImageUrl(),
            user.getProvider().name()
        );
        
        return new AuthResponse(newAccessToken, newRefreshToken.getToken(), userInfo);
    }
    
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.revokeToken(refreshToken);
        }
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
