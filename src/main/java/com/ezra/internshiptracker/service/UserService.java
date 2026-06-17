package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.entity.Role;
import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.repository.InternshipRepository;
import com.ezra.internshiptracker.repository.RefreshTokenRepository;
import com.ezra.internshiptracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezra.internshiptracker.exception.UserNotFoundException;

import com.ezra.internshiptracker.dto.user.LoginRequest;

import com.ezra.internshiptracker.exception.DuplicateUserException;
import com.ezra.internshiptracker.exception.InvalidPasswordException;
import com.ezra.internshiptracker.exception.LoginFailedException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final InternshipRepository internshipRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       InternshipRepository internshipRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.internshipRepository = internshipRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User createUser(User user) {

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateUserException("Username already exists");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateUserException("Email already exists");
        }

        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public User updateUser(Long currentUserId, User newUser) {
        User user = getUserById(currentUserId);

        if (userRepository.existsByUsernameAndIdNot(newUser.getUsername(), user.getId())) {
            throw new DuplicateUserException("Username already exists");
        }

        if (userRepository.existsByEmailAndIdNot(newUser.getEmail(), user.getId())) {
            throw new DuplicateUserException("Email already exists");
        }

        user.setUsername(newUser.getUsername());
        user.setEmail(newUser.getEmail());

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);

        internshipRepository.deleteAll(
                internshipRepository.searchMyInternships(userId, null, null, org.springframework.data.domain.Pageable.unpaged())
        );
        refreshTokenRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    public User login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new LoginFailedException("Username or password is incorrect"));

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!matches) {
            throw new LoginFailedException("Username or password is incorrect");
        }

        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
