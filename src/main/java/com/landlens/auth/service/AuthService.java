package com.landlens.auth.service;

import com.landlens.auth.dto.LoginRequest;
import com.landlens.auth.dto.RefreshTokenRequest;
import com.landlens.auth.dto.RegisterRequest;
import com.landlens.auth.dto.TokenResponse;
import com.landlens.auth.model.LoginHistory;
import com.landlens.auth.model.RefreshToken;
import com.landlens.auth.model.Role;
import com.landlens.auth.repository.LoginHistoryRepository;
import com.landlens.auth.repository.RefreshTokenRepository;
import com.landlens.auth.repository.RoleRepository;
import com.landlens.auth.security.JwtTokenProvider;
import com.landlens.user.model.User;
import com.landlens.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Value("${landlens.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        Role role = roleRepository.findByName(request.getRole().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRole()));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(role);

        // Standard audits will default to system or be null initially as no user is authenticated during signup
        return userRepository.save(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            // Write a failed login history for dummy tracking
            throw new RuntimeException("Bad credentials");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            LoginHistory loginHistory = new LoginHistory();
            loginHistory.setUser(user);
            loginHistory.setIpAddress(ipAddress);
            loginHistory.setUserAgent(userAgent);
            loginHistory.setStatus("FAILED");
            loginHistoryRepository.save(loginHistory);
            throw new RuntimeException("Bad credentials");
        }

        // Save login history
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setUser(user);
        loginHistory.setIpAddress(ipAddress);
        loginHistory.setUserAgent(userAgent);
        loginHistory.setStatus("SUCCESS");
        loginHistoryRepository.save(loginHistory);

        // Generate Access Token
        String accessToken = tokenProvider.generateToken(user);

        // Generate Refresh Token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        refreshTokenRepository.save(refreshToken);

        return new TokenResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                user.getRole().getName(),
                user.getId()
        );
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        if (refreshToken.getRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

        User user = refreshToken.getUser();
        String accessToken = tokenProvider.generateToken(user);

        return new TokenResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                user.getRole().getName(),
                user.getId()
        );
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }
}
