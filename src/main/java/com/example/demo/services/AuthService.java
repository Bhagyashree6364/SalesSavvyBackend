package com.example.demo.services;

import com.example.demo.entities.*;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.JWTTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JWTTokenRepository jwtTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Key SIGNING_KEY;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            JWTTokenRepository jwtTokenRepository,
            @Value("${jwt.secret}") String jwtSecret) {

        this.userRepository = userRepository;
        this.jwtTokenRepository = jwtTokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();

        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < 64) {
            throw new IllegalArgumentException("jwt.secret must be at least 64 bytes");
        }
        this.SIGNING_KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        return user;
    }

    public String generateToken(User user) {
        LocalDateTime now = LocalDateTime.now();
        JWTToken existing = jwtTokenRepository.findByUserId(user.getId());

        String token;
        if (existing != null && now.isBefore(existing.getExpiresAt())) {
            token = existing.getToken();
        } else {
            token = generateNewToken(user);
            if (existing != null) {
                jwtTokenRepository.delete(existing);
            }
            saveToken(user, token);
        }
        return token;
    }

    private String generateNewToken(User user) {
        Date now = new Date();
        Date expiry = new Date(System.currentTimeMillis() + 3600_000L);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    public void saveToken(User user, String token) {
        JWTToken jwtToken = new JWTToken(user, token, LocalDateTime.now().plusHours(1));
        jwtTokenRepository.save(jwtToken);
    }
}
