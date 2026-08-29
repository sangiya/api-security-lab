package com.sangiya.apisec.auth;

import java.util.Objects;

public class LoginResponse {

    private final String accessToken;
    private final String tokenType;
    private final String username;
    private final String role;
    private final long expiresIn;

    public LoginResponse(String accessToken, String tokenType, String username,
                         String role, long expiresIn) {
        this.accessToken = Objects.requireNonNull(accessToken);
        this.tokenType = tokenType;
        this.username = username;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
