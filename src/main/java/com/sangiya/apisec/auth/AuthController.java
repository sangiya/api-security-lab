package com.sangiya.apisec.auth;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sangiya.apisec.error.GlobalExceptionHandler;
import com.sangiya.apisec.jwt.JwtService;
import com.sangiya.apisec.model.UserService;

import io.jsonwebtoken.lang.Assert;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final long accessTokenExpiryMs;
    private final GlobalExceptionHandler exceptionHandler;

    public AuthController(UserService userService,
                          JwtService jwtService,
                          @Value("${app.jwt.access-token-expiry-ms}") long accessTokenExpiryMs,
                          GlobalExceptionHandler exceptionHandler) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.exceptionHandler = exceptionHandler;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Assert.notNull(request, "login request must not be null");
        Optional<UserService.User> authenticated =
                userService.authenticate(request.getUsername(), request.getPassword());
        if (authenticated.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(exceptionHandler.buildInvalidCredentials(
                            "Invalid username or password", "/api/auth/login"));
        }

        UserService.User user = authenticated.get();
        String token = jwtService.generateAccessToken(user.username(),
                Map.of("role", user.role()));
        LoginResponse response = new LoginResponse(token, "Bearer",
                user.username(), user.role(), accessTokenExpiryMs);
        return ResponseEntity.ok(response);
    }
}
