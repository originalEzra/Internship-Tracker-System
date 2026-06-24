package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.config.JwtProperties;
import com.ezra.internshiptracker.config.JwtUtil;
import com.ezra.internshiptracker.entity.RefreshToken;
import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.exception.UnauthorizedException;
import com.ezra.internshiptracker.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class AuthService {

    private static final int REFRESH_TOKEN_BYTES = 32;

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(JwtUtil jwtUtil,
                       JwtProperties jwtProperties,
                       RefreshTokenRepository refreshTokenRepository,
                       TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public String createAccessToken(User user) {
        return jwtUtil.generateToken(user.getId());
    }

    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = generateRawRefreshToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        LocalDateTime now = LocalDateTime.now();

        refreshToken.setCreatedAt(now);
        refreshToken.setExpiresAt(
                now.plus(Duration.ofMillis(jwtProperties.getRefreshExpirationMs()))
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public String refreshAccessToken(String rawRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token expired");
        }

        return jwtUtil.generateToken(refreshToken.getUser().getId());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.deleteByTokenHash(hashToken(rawRefreshToken));
    }

    @Transactional
    public void logout(String rawRefreshToken, String rawAccessToken) {
        logout(rawRefreshToken);
        tokenBlacklistService.blacklistAccessToken(rawAccessToken);
    }

    @Transactional
    public void deleteRefreshTokensForUser(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private String generateRawRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
