package com.ikobana.backend.service;

import com.ikobana.backend.entity.User;
import com.ikobana.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Custom UserDetailsService implementation for loading user credentials and authorities from the database.
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by registered email or username for Spring Security authentication.
     *
     * @param email user email address (or username)
     * @return UserDetails populated with user credentials and roles
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading UserDetails by email/username: {}", email);
        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findByUsername(email))
                .orElseThrow(() -> {
                    log.warn("User not found with identifier: {}", email);
                    return new UsernameNotFoundException("User tidak ditemukan dengan email: " + email);
                });

        return buildUserDetails(user);
    }

    /**
     * Loads a user by database ID for JWT token authentication filter.
     *
     * @param id user database ID
     * @return UserDetails populated with user credentials and roles
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        log.debug("Loading UserDetails by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan dengan id: " + id));

        return buildUserDetails(user);
    }

    private UserDetails buildUserDetails(User user) {
        String roleStr = user.getRole() != null ? user.getRole() : "CUSTOMER";
        String authorityName = roleStr.startsWith("ROLE_") ? roleStr : "ROLE_" + roleStr;

        boolean isEnabled = Boolean.TRUE.equals(user.getIsActive());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                isEnabled,
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority(authorityName))
        );
    }
}
