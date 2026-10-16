package com.ikobana.backend.controller;

import com.ikobana.backend.dto.ApiResponse;
import com.ikobana.backend.dto.AuthResponse;
import com.ikobana.backend.dto.LoginRequest;
import com.ikobana.backend.dto.RegisterRequest;
import com.ikobana.backend.dto.UserDTO;
import com.ikobana.backend.entity.User;
import com.ikobana.backend.entity.UserRole;
import com.ikobana.backend.exception.DuplicateEmailException;
import com.ikobana.backend.exception.InvalidCredentialsException;
import com.ikobana.backend.exception.UserNotFoundException;
import com.ikobana.backend.repository.UserRepository;
import com.ikobana.backend.security.JwtTokenProvider;
import com.ikobana.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints: registration, login, profile, and logout.
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public AuthController(
            AuthService authService,
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository,
            ModelMapper modelMapper
    ) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Registers a new user account.
     *
     * @param request registration request data
     * @return ApiResponse with created AuthResponse or error status
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody @Valid RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", response));
        } catch (DuplicateEmailException ex) {
            log.warn("Registration rejected: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Authenticates user credentials and issues a JWT token.
     *
     * @param request login credentials
     * @return ApiResponse with AuthResponse containing JWT or error status
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody @Valid LoginRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (UserNotFoundException | InvalidCredentialsException ex) {
            // Pesan & status sama untuk email tidak terdaftar maupun password salah,
            // supaya penyerang tidak bisa menebak email mana yang terdaftar.
            log.warn("Login failed for email '{}': {}", request.getEmail(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Email atau password salah"));
        }
    }

    /**
     * Retrieves currently authenticated user profile.
     *
     * @param userDetails authenticated principal
     * @return ApiResponse with UserDTO
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User tidak terotentikasi"));
        }

        log.info("Fetching profile for authenticated user: {}", userDetails.getUsername());
        User user = userRepository.findByEmail(userDetails.getUsername())
                .or(() -> userRepository.findByUsername(userDetails.getUsername()))
                .orElseThrow(() -> new UserNotFoundException("User tidak ditemukan"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        if (user.getRole() != null) {
            try {
                userDTO.setRole(UserRole.valueOf(user.getRole()));
            } catch (IllegalArgumentException e) {
                userDTO.setRole(UserRole.CUSTOMER);
            }
        }

        return ResponseEntity.ok(ApiResponse.success("User detail", userDTO));
    }

    /**
     * Logs out the current user and invalidates the bearer token.
     *
     * @param authHeader HTTP Authorization header
     * @return ApiResponse with success message
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader != null && !authHeader.isBlank()) {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            authService.logout(token);
        }
        log.info("User logout completed successfully");
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }
}
