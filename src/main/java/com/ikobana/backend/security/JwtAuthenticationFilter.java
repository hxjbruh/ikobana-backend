package com.ikobana.backend.security;

import com.ikobana.backend.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that intercepts incoming HTTP requests to validate JWT bearer tokens
 * and populate Spring Security's {@link SecurityContextHolder}.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklist tokenBlacklist;

    @Autowired
    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService customUserDetailsService,
            TokenBlacklist tokenBlacklist
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Token yang sudah di-logout ditolak walaupun tanda tangannya masih valid.
                if (jwtTokenProvider.validateToken(token) && !tokenBlacklist.contains(token)) {
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);
                    UserDetails userDetails = customUserDetailsService.loadUserById(userId);

                    if (userDetails != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Authenticated user ID {} via JWT token", userId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context: {}", e.getMessage());
            // Do not throw; continue filter chain to let AuthenticationEntryPoint handle unauthorized access
        }

        filterChain.doFilter(request, response);
    }
}
