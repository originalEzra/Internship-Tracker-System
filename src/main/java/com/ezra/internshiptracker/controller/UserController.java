package com.ezra.internshiptracker.controller;

import com.ezra.internshiptracker.entity.User;
import org.springframework.web.bind.annotation.*;

//往下转入dto
import com.ezra.internshiptracker.dto.user.UserResponse;

//让UserService接管，controller不再直接操作数据库，Service调用Repository完成crud操作
import com.ezra.internshiptracker.dto.user.RefreshTokenRequest;
import com.ezra.internshiptracker.dto.user.RefreshTokenResponse;
import com.ezra.internshiptracker.service.AuthService;
import com.ezra.internshiptracker.service.UserService;
import com.ezra.internshiptracker.dto.user.UserCreateRequest;
import com.ezra.internshiptracker.dto.user.UserPasswordUpdateRequest;
import com.ezra.internshiptracker.dto.user.UserUpdateRequest;
import com.ezra.internshiptracker.dto.ApiResponse;

import com.ezra.internshiptracker.dto.user.LoginRequest;

import com.ezra.internshiptracker.dto.user.LoginResponse;

import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

/*  private final UserRepository userRepository; //声明一个UserRepository对象作为成员变量

    public UserController(UserRepository userRepository) {
        //形参名无所谓 变量类型必须一致 spring会自动把真实的一个UserRepository对象传进来，它具备所有UserRepository的功能
        this.userRepository = userRepository;
    }   */

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    private UserResponse toUserResponse(User user) { //DTO
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    private Long getCurrentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        return authorizationHeader.substring(7);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(
            Authentication authentication
    ) {
        User user = userService.getUserById(getCurrentUserId(authentication));

        return ApiResponse.success(toUserResponse(user));
    }

    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        User savedUser = userService.createUser(user);

        UserResponse response = toUserResponse(savedUser);

        return ApiResponse.success("User created", response);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        User user = userService.login(request);

        String token = authService.createAccessToken(user);
        String refreshToken = authService.createRefreshToken(user);

        UserResponse userResponse = toUserResponse(user);

        LoginResponse response =
                new LoginResponse(token, refreshToken, userResponse);

        return ApiResponse.success("Login successful", response);
    }

    @PostMapping("/refresh-token")
    public ApiResponse<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        String token = authService.refreshAccessToken(request.getRefreshToken());

        return ApiResponse.success("Token refreshed", new RefreshTokenResponse(token));
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        authService.logout(request.getRefreshToken(), extractBearerToken(authorizationHeader));

        return ApiResponse.success("Logged out", null);
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateCurrentUser(
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication
    ) {
        User newUser = new User();

        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());

        User updatedUser = userService.updateUser(getCurrentUserId(authentication), newUser);

        return ApiResponse.success("User updated", toUserResponse(updatedUser));
    }

    @PutMapping("/me/password")
    public ApiResponse<String> updateCurrentUserPassword(
            @Valid @RequestBody UserPasswordUpdateRequest request,
            Authentication authentication
    ) {
        userService.updatePassword(
                getCurrentUserId(authentication),
                request.getOldPassword(),
                request.getNewPassword()
        );

        return ApiResponse.success("Password updated", null);
    }

    @DeleteMapping("/me")
    public ApiResponse<String> deleteCurrentUser(
            Authentication authentication
    ) {
        userService.deleteUser(getCurrentUserId(authentication));

        return ApiResponse.success("User deleted", null);
    }
}
