package com.sangiya.apisec.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sangiya.apisec.error.JwtAuthenticationException;

class JwtServiceTest {

    private static final String SECRET =
            "a-very-long-test-secret-that-exceeds-thirty-two-bytes-for-hs256-0123456789";
    private static final long EXPIRY_MS = 900_000;
    private static final String ISSUER = "api-security-lab";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRY_MS, ISSUER);
    }

    @Test
    void generateThenParse_returnsSubjectAndClaims() {
        String token = jwtService.generateAccessToken("alice", Map.of("role", "ADMIN"));

        String subject = jwtService.extractUsername(token);
        String role = jwtService.parseClaims(token).get("role", String.class);

        assertThat(subject).isEqualTo("alice");
        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    void parse_reportsIssuerAndExpiration() {
        String token = jwtService.generateAccessToken("bob", Map.of());

        String issuer = jwtService.parseClaims(token).getIssuer();
        Date expiration = jwtService.extractExpiration(token);

        assertThat(issuer).isEqualTo(ISSUER);
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    void validToken_withMatchingUser_isValid() {
        String token = jwtService.generateAccessToken("carol", Map.of("role", "USER"));

        assertThat(jwtService.isTokenValid(token, "carol")).isTrue();
    }

    @Test
    void validToken_withDifferentUser_isInvalid() {
        String token = jwtService.generateAccessToken("carol", Map.of("role", "USER"));

        assertThat(jwtService.isTokenValid(token, "someone-else")).isFalse();
    }

    @Test
    void expiredToken_isRejectedOnParse() {
        JwtService shortLived = new JwtService(SECRET, -1000, ISSUER);
        String token = shortLived.generateAccessToken("dave", Map.of("role", "USER"));

        assertThat(shortLived.isTokenValid(token, "dave")).isFalse();
    }

    @Test
    void tamperedToken_isRejected() {
        String token = jwtService.generateAccessToken("erin", Map.of("role", "USER"));
        String tampered = token.substring(0, token.length() - 2) + "XX";

        assertThatThrownBy(() -> jwtService.parseClaims(tampered))
                .isInstanceOf(JwtAuthenticationException.class);
    }

    @Test
    void tokenSignedWithAnotherKey_isRejected() {
        String otherSecret = "a-completely-different-secret-key-longer-than-32-bytes-xyz";
        JwtService other = new JwtService(otherSecret, EXPIRY_MS, ISSUER);
        String token = other.generateAccessToken("frank", Map.of("role", "USER"));

        assertThatThrownBy(() -> jwtService.parseClaims(token))
                .isInstanceOf(JwtAuthenticationException.class);
    }
}
