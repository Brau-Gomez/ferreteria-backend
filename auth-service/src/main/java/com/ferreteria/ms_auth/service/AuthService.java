package com.ferreteria.ms_auth.service;

import com.ferreteria.ms_auth.config.JwtService;
import com.ferreteria.ms_auth.dto.AuthResponseDTO;
import com.ferreteria.ms_auth.dto.ValidacionTokenDTO;
import com.ferreteria.ms_auth.model.RefreshToken;
import com.ferreteria.ms_auth.model.User;
import com.ferreteria.ms_auth.repository.RefreshTokenRepository;
import com.ferreteria.ms_auth.exception.BadRequestException;
import com.ferreteria.ms_auth.event.AuthEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AuthResponseDTO login(String email, String password, String clientIp) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername());

        String accessToken = jwtService.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                List.copyOf(user.getRoles())
        );
        String refreshToken = jwtService.generateRefreshToken(user.getId().toString());

        RefreshToken rt = RefreshToken.builder()
                .token(refreshToken)
                .userId(user.getId())
                .expiresAt(jwtService.getExpiration(refreshToken))
                .createdByIp(clientIp)
                .build();
        refreshTokenRepository.save(rt);

        AuthResponseDTO response = buildAuthResponse(accessToken, refreshToken, user);
        eventPublisher.publishEvent(AuthEvents.AuthUserLoginEvent.of(user));
        return response;
    }

    @Transactional
    public AuthResponseDTO register(String username, String email, String password, String fullName, String phone) {
        User user = userService.register(username, email, password, fullName, phone);

        String accessToken = jwtService.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                List.copyOf(user.getRoles())
        );
        String refreshToken = jwtService.generateRefreshToken(user.getId().toString());

        RefreshToken rt = RefreshToken.builder()
                .token(refreshToken)
                .userId(user.getId())
                .expiresAt(jwtService.getExpiration(refreshToken))
                .build();
        refreshTokenRepository.save(rt);

        AuthResponseDTO response = buildAuthResponse(accessToken, refreshToken, user);
        eventPublisher.publishEvent(AuthEvents.AuthUserRegisteredEvent.of(user));
        return response;
    }

    @Transactional
    public AuthResponseDTO refreshToken(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Token inválido: no es un refresh token");
        }

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new BadRequestException("Refresh token expirado");
        }

        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BadRequestException("Refresh token no encontrado"));

        if (!stored.isValid()) {
            throw new BadRequestException("Refresh token revocado o expirado");
        }

        String subject = jwtService.getSubject(refreshToken);
        User user = userService.findById(Long.parseLong(subject));

        // Revocar el refresh token usado (rotación)
        stored.setRevokedAt(Instant.now());
        refreshTokenRepository.save(stored);

        // Generar nuevos tokens
        String newAccessToken = jwtService.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                List.copyOf(user.getRoles())
        );
        String newRefreshToken = jwtService.generateRefreshToken(user.getId().toString());

        RefreshToken newRt = RefreshToken.builder()
                .token(newRefreshToken)
                .userId(user.getId())
                .expiresAt(jwtService.getExpiration(newRefreshToken))
                .build();
        refreshTokenRepository.save(newRt);

        AuthResponseDTO response = buildAuthResponse(newAccessToken, newRefreshToken, user);
        eventPublisher.publishEvent(AuthEvents.AuthTokenRefreshedEvent.of(user.getId()));
        return response;
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.revokeByToken(refreshToken, Instant.now());
        }
        eventPublisher.publishEvent(AuthEvents.AuthUserLogoutEvent.now());
    }

    @Transactional
    public void migratePassword(String email, String plainPassword) {
        User user = userService.findByEmail(email);
        userService.migratePassword(user.getId(), plainPassword);
        // Revocar todos los refresh tokens del usuario tras migración de contraseña
        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
    }

    public ValidacionTokenDTO validateToken(String accessToken) {
        if (!jwtService.isAccessToken(accessToken)) {
            return ValidacionTokenDTO.builder().valido(false).build();
        }

        if (jwtService.isTokenExpired(accessToken)) {
            return ValidacionTokenDTO.builder().valido(false).build();
        }

        return ValidacionTokenDTO.builder()
                .valido(true)
                .username(jwtService.getSubject(accessToken))
                .email(jwtService.getEmail(accessToken))
                .roles(java.util.Set.copyOf(jwtService.getRoles(accessToken)))
                .build();
    }

    private AuthResponseDTO buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration(accessToken).toEpochMilli() - System.currentTimeMillis())
                .refreshExpiresAt(jwtService.getExpiration(refreshToken))
                .user(AuthResponseDTO.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .roles(user.getRoles())
                        .build())
                .build();
    }
}