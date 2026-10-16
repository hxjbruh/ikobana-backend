package com.ikobana.backend.service;

import com.ikobana.backend.dto.AuthData;
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
import com.ikobana.backend.security.TokenBlacklist;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service handling authentication operations: registration, login, token validation, and logout.
 */
@Service
@Transactional
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ModelMapper modelMapper;

    @Value("${jwt.expiration:86400}")
    private long jwtExpiration;

    private final TokenBlacklist tokenBlacklist;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            ModelMapper modelMapper,
            TokenBlacklist tokenBlacklist
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.modelMapper = modelMapper;
        this.tokenBlacklist = tokenBlacklist;
    }

    /**
     * Registers a new user account.
     *
     * @param request registration details
     * @return AuthResponse containing generated JWT token and user info
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing user registration for email: {}", request.getEmail());

        if (request.getPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidCredentialsException("Password dan konfirmasi password tidak cocok");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration rejected: Duplicate email '{}'", request.getEmail());
            throw new DuplicateEmailException("Email " + request.getEmail() + " sudah terdaftar");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration rejected: Duplicate username '{}'", request.getUsername());
            throw new DuplicateEmailException("Username " + request.getUsername() + " sudah digunakan");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // Registrasi publik selalu menjadi CUSTOMER; role dari request diabaikan supaya
        // tidak ada yang bisa mendaftarkan dirinya sendiri sebagai ADMIN.
        user.setRole(UserRole.CUSTOMER.name());
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        log.info("Successfully registered user ID: {}", savedUser.getId());

        String token = jwtTokenProvider.generateToken(savedUser);
        return AuthResponse.success("Registrasi berhasil", buildAuthData(savedUser, token));
    }

    /**
     * Authenticates a user with email and password.
     *
     * @param request login credentials
     * @return AuthResponse containing generated JWT token and user info
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found with email '{}'", request.getEmail());
                    return new UserNotFoundException("User dengan email " + request.getEmail() + " tidak ditemukan");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: Invalid password for email '{}'", request.getEmail());
            throw new InvalidCredentialsException("Password salah");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            log.warn("Login failed: Account is deactivated for email '{}'", request.getEmail());
            throw new InvalidCredentialsException("Akun tidak aktif, silakan hubungi admin");
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user);
        log.info("User ID {} successfully logged in", user.getId());

        return AuthResponse.success("Login berhasil", buildAuthData(user, token));
    }

    /**
     * Validates a JWT token and returns the corresponding User entity.
     *
     * @param token JWT token string
     * @return Optional containing the User if token is valid and user exists
     */
    @Transactional(readOnly = true)
    public Optional<User> validateToken(String token) {
        if (token == null || tokenBlacklist.contains(token)) {
            return Optional.empty();
        }

        if (!jwtTokenProvider.validateToken(token)) {
            return Optional.empty();
        }

        try {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            return userRepository.findById(userId);
        } catch (Exception ex) {
            log.error("Failed to extract user from token: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Invalidates a token by adding it to the blacklist until it would have expired.
     * The JWT filter rejects blacklisted tokens, so the token stops working immediately.
     *
     * @param token JWT token string to invalidate
     */
    public void logout(String token) {
        if (token != null && !token.isBlank() && jwtTokenProvider.validateToken(token)) {
            tokenBlacklist.add(token, jwtTokenProvider.getExpirationMillis(token));
            log.info("Token successfully invalidated / logged out");
        }
    }

    /**
     * Converts a User entity and token into an AuthData DTO.
     */
    private AuthData buildAuthData(User user, String token) {
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        if (user.getRole() != null) {
            try {
                userDTO.setRole(UserRole.valueOf(user.getRole()));
            } catch (IllegalArgumentException e) {
                userDTO.setRole(UserRole.CUSTOMER);
            }
        }
        return AuthData.of(token, userDTO, jwtExpiration);
    }
}
