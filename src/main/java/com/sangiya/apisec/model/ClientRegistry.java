package com.sangiya.apisec.model;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Simulated OAuth2 client registry for the client-credentials grant.
 * Registered service principals are stored with BCrypt-hashed secrets; no
 * plaintext secret persists in memory after startup.
 */
@Service
public class ClientRegistry {

    public record Client(String clientId, String clientSecretHash, String role, String scope) {
    }

    private final Map<String, Client> clients = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;

    public ClientRegistry() {
        this.passwordEncoder = new BCryptPasswordEncoder();
        seedClients();
    }

    /**
     * Returns the client if credentials match, otherwise null.
     */
    public Client authenticate(String clientId, String clientSecret) {
        Client client = clients.get(clientId);
        if (client == null) {
            return null;
        }
        if (!passwordEncoder.matches(clientSecret, client.clientSecretHash())) {
            return null;
        }
        return client;
    }

    private void seedClients() {
        clients.put("service-a", new Client("service-a",
                passwordEncoder.encode("secret-a"), "SERVICE", "read:accounts"));
        clients.put("service-b", new Client("service-b",
                passwordEncoder.encode("secret-b"), "SERVICE", "write:transfers"));
    }
}
