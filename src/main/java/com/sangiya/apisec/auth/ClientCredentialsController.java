package com.sangiya.apisec.auth;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sangiya.apisec.jwt.JwtService;
import com.sangiya.apisec.model.ClientRegistry;

import jakarta.validation.Valid;

/**
 * Simulated OAuth2 client-credentials grant. A registered service exchanges
 * its client_id/client_secret for a short-lived bearer token scoped to that
 * client, mirroring how machine-to-machine integrations authenticate.
 */
@RestController
@RequestMapping("/api/auth")
public class ClientCredentialsController {

    private final ClientRegistry clientRegistry;
    private final JwtService jwtService;

    public ClientCredentialsController(ClientRegistry clientRegistry, JwtService jwtService) {
        this.clientRegistry = clientRegistry;
        this.jwtService = jwtService;
    }

    @PostMapping("/token")
    public ResponseEntity<?> token(@Valid @RequestBody ClientCredentialsRequest request) {
        ClientRegistry.Client client = clientRegistry
                .authenticate(request.getClientId(), request.getClientSecret());
        if (client == null) {
            String body = "{\"error\":\"invalid_client\",\"error_description\":\"Invalid client credentials\"}";
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        String token = jwtService.generateAccessToken(client.clientId(),
                Map.of("role", client.role(), "scope", "service"));
        return ResponseEntity.ok(new ClientTokenResponse(token, "Bearer",
                client.clientId(), client.scope(), milliseconds()));
    }

    private long milliseconds() {
        return 3_600_000L;
    }

    private static final class ClientTokenResponse {
        private final String accessToken;
        private final String tokenType;
        private final String clientId;
        private final String scope;
        private final long expiresIn;

        ClientTokenResponse(String accessToken, String tokenType, String clientId,
                            String scope, long expiresIn) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.clientId = clientId;
            this.scope = scope;
            this.expiresIn = expiresIn;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public String getClientId() {
            return clientId;
        }

        public String getScope() {
            return scope;
        }

        public long getExpiresIn() {
            return expiresIn;
        }
    }
}
