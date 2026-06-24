package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.dto.user.LoginRequest;
import com.ezra.internshiptracker.entity.Internship;
import com.ezra.internshiptracker.entity.Role;
import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.exception.DuplicateUserException;
import com.ezra.internshiptracker.exception.InvalidPasswordException;
import com.ezra.internshiptracker.exception.LoginFailedException;
import com.ezra.internshiptracker.repository.InternshipRepository;
import com.ezra.internshiptracker.repository.RefreshTokenRepository;
import com.ezra.internshiptracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InternshipRepository internshipRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoginRateLimitService loginRateLimitService;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserHashesPasswordBeforeSaving() {
        User user = user("ezra", "ezra@example.com", "raw-password");

        when(userRepository.existsByUsername("ezra")).thenReturn(false);
        when(userRepository.existsByEmail("ezra@example.com")).thenReturn(false);
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.createUser(user);

        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        verify(passwordEncoder).encode("raw-password");
        verify(userRepository).save(user);
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        User user = user("ezra", "ezra@example.com", "password");

        when(userRepository.existsByUsername("ezra")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginReturnsUserWhenPasswordMatches() {
        LoginRequest request = loginRequest("ezra", "raw-password");
        User user = user("ezra", "ezra@example.com", "encoded-password");

        when(userRepository.findByUsername("ezra")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);

        User loggedInUser = userService.login(request);

        assertThat(loggedInUser).isSameAs(user);
        verify(loginRateLimitService).checkAllowed("ezra");
        verify(loginRateLimitService).clearFailures("ezra");
    }

    @Test
    void loginRejectsWrongPassword() {
        LoginRequest request = loginRequest("ezra", "wrong-password");
        User user = user("ezra", "ezra@example.com", "encoded-password");

        when(userRepository.findByUsername("ezra")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("Username or password is incorrect");

        verify(loginRateLimitService).checkAllowed("ezra");
        verify(loginRateLimitService).recordFailure("ezra");
    }

    @Test
    void updatePasswordRequiresOldPasswordToMatch() {
        User user = user("ezra", "ezra@example.com", "encoded-old-password");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-old-password", "encoded-old-password")).thenReturn(false);

        assertThatThrownBy(() -> userService.updatePassword(1L, "wrong-old-password", "new-password"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("Old password is incorrect");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePasswordStoresEncodedNewPassword() {
        User user = user("ezra", "ezra@example.com", "encoded-old-password");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "encoded-old-password")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

        userService.updatePassword(1L, "old-password", "new-password");

        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
        verify(userRepository).save(user);
    }

    @Test
    void deleteUserDeletesOwnedInternshipsBeforeDeletingUser() {
        User user = user("ezra", "ezra@example.com", "password");
        Internship internship = new Internship();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(internshipRepository.searchMyInternships(eq(1L), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(internship)));

        userService.deleteUser(1L);

        ArgumentCaptor<Iterable<Internship>> internshipsCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(internshipRepository).deleteAll(internshipsCaptor.capture());
        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(userRepository).delete(user);
        assertThat(internshipsCaptor.getValue()).containsExactly(internship);
    }

    private User user(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
