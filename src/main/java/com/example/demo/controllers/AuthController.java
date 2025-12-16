package com.example.demo.controllers;

import com.example.demo.dto.LoginRequest;
import com.example.demo.entities.User;
import com.example.demo.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
                                   HttpServletResponse response) {
        try {
            User user = authService.authenticate(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            // Generate JWT token
            String token = authService.generateToken(user);

            // Set JWT as HttpOnly cookie
            Cookie cookie = new Cookie("authToken", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // set true in production with HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(3600); // 1 hour
            response.addCookie(cookie);

            // Return token + role + username in JSON body
            Map<String, Object> body = new HashMap<>();
            body.put("message", "Login successful");
            body.put("token", token);
            body.put("username", user.getUsername());
            body.put("role", user.getRole().name());

            return ResponseEntity.ok(body);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response) {
        try {
            // Get authenticated user from AuthenticationFilter (may be null if not set)
            User user = (User) request.getAttribute("authenticatedUser");

            // Optional: invalidate token(s) in DB if you store them
            authService.logout(user);

            // Clear authToken cookie
            Cookie cookie = new Cookie("authToken", null);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            Map<String, Object> body = new HashMap<>();
            body.put("message", "Logout successful");
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", "Logout failed");
            return ResponseEntity.status(500).body(body);
        }
    }
}

