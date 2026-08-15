package com.restaurant.ai.controller;

import com.restaurant.ai.dto.AuthTokenResponse;
import com.restaurant.ai.dto.LoginRequest;
import com.restaurant.ai.dto.RefreshRequest;
import com.restaurant.ai.security.AppUserDetailsService;
import com.restaurant.ai.security.AppUserPrincipal;
import com.restaurant.ai.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Stateless JWT auth - logout is a client-side token discard (no server-side
 * session/blacklist in this MVP). Refresh tokens are not stored/revocable yet;
 * that's a reasonable Phase 25 hardening item once real users are on the system.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, AppUserDetailsService userDetailsService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).build();
        }

        AppUserPrincipal principal = (AppUserPrincipal) userDetailsService.loadUserByUsername(request.email());
        String accessToken = jwtService.issueAccessToken(principal);
        String refreshToken = jwtService.issueRefreshToken(principal);
        return ResponseEntity.ok(new AuthTokenResponse(accessToken, refreshToken, principal.getRoleName(), principal.getUsername()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            Claims claims = jwtService.parseAndValidate(request.refreshToken());
            if (!jwtService.isRefreshToken(claims)) {
                return ResponseEntity.status(401).build();
            }
            AppUserPrincipal principal = (AppUserPrincipal) userDetailsService.loadUserByUsername(claims.getSubject());
            String accessToken = jwtService.issueAccessToken(principal);
            String newRefreshToken = jwtService.issueRefreshToken(principal);
            return ResponseEntity.ok(new AuthTokenResponse(accessToken, newRefreshToken, principal.getRoleName(), principal.getUsername()));
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Stateless tokens: nothing to invalidate server-side yet. The client discards its tokens.
        return ResponseEntity.ok(Map.of("loggedOut", true));
    }
}
