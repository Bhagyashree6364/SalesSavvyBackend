package com.example.demo.filters;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.AuthService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@WebFilter(urlPatterns = {"/api/*", "/admin/*"})
@Component
public class AuthenticationFilter implements Filter {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthenticationFilter.class);

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";

    private static final String[] UNAUTHENTICATED_PATHS = {
            "/api/users/register",
            "/api/auth/login",
            "/error"
    };

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthenticationFilter(AuthService authService,
                                UserRepository userRepository) {
        System.out.println("AuthenticationFilter started");
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        try {
            executeFilterLogic(request, response, chain);
        } catch (Exception e) {
            logger.error("Unexpected error in AuthenticationFilter", e);
            sendErrorResponse((HttpServletResponse) response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error");
        }
    }

    private void executeFilterLogic(ServletRequest request,
                                    ServletResponse response,
                                    FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        logger.info("Request URI: {}", requestURI);

        // 1. Allow unauthenticated paths
        if (Arrays.asList(UNAUTHENTICATED_PATHS).contains(requestURI)) {
            setCORSHeaders(httpResponse);
            chain.doFilter(request, response);
            return;
        }

        // 2. Handle preflight
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            setCORSHeaders(httpResponse);
            return;
        }

        // 3. Extract token from Authorization header OR cookie
        String token = getTokenFromAuthorizationHeader(httpRequest);
        if (token == null) {
            token = getAuthTokenFromCookies(httpRequest);
        }

        logger.info("Token found: {}", token != null);

        if (token == null || !authService.validateToken(token)) {
            sendErrorResponse(httpResponse,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized: Invalid or missing token");
            return;
        }

        // 4. Get username from token and load user
        String username = authService.extractUsername(token);
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            sendErrorResponse(httpResponse,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized: User not found");
            return;
        }

        // 5. Get authenticated user and role
        User authenticatedUser = userOptional.get();
        Role role = authenticatedUser.getRole();
        logger.info("Authenticated user: {}, role: {}", authenticatedUser.getUsername(), role);

        // 6. Build Spring Security Authentication with authorities
        //    Authority string must match what you use in hasAnyAuthority(...)
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(role.name())); // "CUSTOMER" or "ADMIN"

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser, // principal
                        null,              // no credentials
                        authorities
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 7. Role-based access for /admin URLs (optional; Spring rules can also handle this)
        logger.info("URI: {} , role: {}", requestURI, role);

        if (requestURI.startsWith("/admin/") || requestURI.startsWith("/api/admin")) {
            logger.info("Admin URL hit");
            if (role != Role.ADMIN) {
                logger.warn("Blocking request: non-admin trying to access admin URL");
                sendErrorResponse(httpResponse,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Forbidden: Admin access required");
                return;
            }
        } else {
            logger.info("Non-admin URL, allowing for role {}", role);
        }

        // 8. Attach user to request for controllers
        httpRequest.setAttribute("authenticatedUser", authenticatedUser);

        setCORSHeaders(httpResponse);
        chain.doFilter(request, response);
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
        response.setHeader("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers",
                "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    private void sendErrorResponse(HttpServletResponse response,
                                   int statusCode,
                                   String message) throws IOException {
        response.setStatus(statusCode);
        response.getWriter().write(message);
    }

    private String getAuthTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> "authToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private String getTokenFromAuthorizationHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
