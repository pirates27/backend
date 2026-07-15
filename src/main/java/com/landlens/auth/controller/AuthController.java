package com.landlens.auth.controller;

import com.landlens.auth.dto.LoginRequest;
import com.landlens.auth.dto.RefreshTokenRequest;
import com.landlens.auth.dto.RegisterRequest;
import com.landlens.auth.dto.TokenResponse;
import com.landlens.auth.service.AuthService;
import com.landlens.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = authService.register(registerRequest);
        return ResponseEntity.ok("User registered successfully with ID: " + user.getId());
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        
        // Handle proxies
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            ipAddress = xForwardedFor.split(",")[0].trim();
        }

        TokenResponse tokenResponse = authService.login(loginRequest, ipAddress, userAgent);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        TokenResponse tokenResponse = authService.refresh(refreshTokenRequest);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        authService.logout(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok("User logged out successfully");
    }
}
