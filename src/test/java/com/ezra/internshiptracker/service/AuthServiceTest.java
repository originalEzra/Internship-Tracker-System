package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.config.JwtProperties;
import com.ezra.internshiptracker.config.JwtUtil;
import com.ezra.internshiptracker.entity.RefreshToken;
import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.exception.UnauthorizedException;
import com.ezra.internshiptracker.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void createRefreshTokenStoresHashInsteadOfRawToken() {
        AuthService authService = authService();
        User user = user(1L);

        String rawRefreshToken = authService.createRefreshToken(user);

        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

        RefreshToken savedToken = refreshTokenCaptor.getValue();
        assertThat(rawRefreshToken).hasSize(64);
        assertThat(savedToken.getTokenHash()).hasSize(64);
        assertThat(savedToken.getTokenHash()).isNotEqualTo(rawRefreshToken);
        assertThat(savedToken.getUser()).isSameAs(user);
        assertThat(savedToken.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void refreshAccessTokenReturnsNewAccessTokenForValidRefreshToken() {
        AuthService authService = authService();
        User user = user(7L);
        RefreshToken refreshToken = refreshToken(user, LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));
        when(jwtUtil.generateToken(7L)).thenReturn("new-access-token");

        String accessToken = authService.refreshAccessToken("raw-refresh-token");

        assertThat(accessToken).isEqualTo("new-access-token");
    }

    @Test
    void refreshAccessTokenRejectsAndDeletesExpiredRefreshToken() {
        AuthService authService = authService();
        RefreshToken refreshToken = refreshToken(user(1L), LocalDateTime.now().minusMinutes(1));

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.refreshAccessToken("raw-refresh-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Refresh token expired");

        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void logoutDeletesRefreshTokenByHash() {
        AuthService authService = authService();

        authService.logout("raw-refresh-token");

        ArgumentCaptor<String> tokenHashCaptor = ArgumentCaptor.forClass(String.class);
        verify(refreshTokenRepository).deleteByTokenHash(tokenHashCaptor.capture());

        assertThat(tokenHashCaptor.getValue()).hasSize(64);
        assertThat(tokenHashCaptor.getValue()).isNotEqualTo("raw-refresh-token");
    }

    @Test
    void logoutWithAccessTokenBlacklistsAccessToken() {
        AuthService authService = authService();

        authService.logout("raw-refresh-token", "raw-access-token");

        verify(refreshTokenRepository).deleteByTokenHash(anyString());
        verify(tokenBlacklistService).blacklistAccessToken("raw-access-token");
    }

    private AuthService authService() {
        return new AuthService(jwtUtil, jwtProperties(), refreshTokenRepository, tokenBlacklistService);
    }

    private JwtProperties jwtProperties() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-with-at-least-32-bytes");
        jwtProperties.setExpirationMs(3_600_000L);
        jwtProperties.setRefreshExpirationMs(604_800_000L);
        return jwtProperties;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private RefreshToken refreshToken(User user, LocalDateTime expiresAt) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash("hash");
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setExpiresAt(expiresAt);
        return refreshToken;
    }
}
