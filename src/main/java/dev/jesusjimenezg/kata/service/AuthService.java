package dev.jesusjimenezg.kata.service;

import dev.jesusjimenezg.kata.dto.AuthResponse;
import dev.jesusjimenezg.kata.dto.LoginRequest;
import dev.jesusjimenezg.kata.dto.RefreshRequest;
import dev.jesusjimenezg.kata.dto.RegisterRequest;
import dev.jesusjimenezg.kata.model.AppUser;
import dev.jesusjimenezg.kata.model.RefreshToken;
import dev.jesusjimenezg.kata.model.Role;
import dev.jesusjimenezg.kata.repository.AppUserRepository;
import dev.jesusjimenezg.kata.repository.RefreshTokenRepository;
import dev.jesusjimenezg.kata.repository.RoleRepository;
import dev.jesusjimenezg.kata.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "USER";

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final long refreshTokenExpirationMs;

    public AuthService(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }

        String roleName = (request.role() != null && !request.role().isBlank())
                ? request.role().toUpperCase()
                : DEFAULT_ROLE;

        Role userRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + roleName));

        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRoles(Set.of(userRole));
        appUserRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return buildAuthResponse(userDetails, user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        return buildAuthResponse(userDetails, user);
    }

    @Transactional
    public void logout(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String tokenHash = hashToken(request.refreshToken());

        RefreshToken storedToken = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or revoked refresh token"));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new IllegalArgumentException("Refresh token expired");
        }

        // Rotate: revoke old, issue new
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        AppUser user = storedToken.getUser();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null,
                user.getRoles().stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_" + role.getName()))
                        .toList());

        // Build response using the user directly (no re-auth needed)
        String accessToken = jwtService.generateAccessToken(
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(), "", authentication.getAuthorities()));
        String rawRefreshToken = generateAndStoreRefreshToken(user);

        return new AuthResponse(accessToken, rawRefreshToken, user.getEmail());
    }

    private AuthResponse buildAuthResponse(UserDetails userDetails, AppUser user) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String rawRefreshToken = generateAndStoreRefreshToken(user);
        return new AuthResponse(accessToken, rawRefreshToken, user.getEmail());
    }

    private String generateAndStoreRefreshToken(AppUser user) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000));
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
