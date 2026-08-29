package com.sangiya.apisec.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * In-memory user registry. Hashes are generated at startup with BCrypt so no
 * plaintext password is ever stored. Swap for a real user store (DB/LDAP) in
 * production without changing callers.
 */
@Service
public class UserService {

    public record User(String username, String passwordHash, String role) {
    }

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;

    public UserService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
        seedUsers();
    }

    public Optional<User> authenticate(String username, String rawPassword) {
        User user = users.get(username);
        if (user == null) {
            return Optional.empty();
        }
        if (!passwordEncoder.matches(rawPassword, user.passwordHash())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public List<String> allUsernames() {
        return List.copyOf(users.keySet());
    }

    private void seedUsers() {
        users.put("admin", new User("admin", passwordEncoder.encode("admin123"), "ADMIN"));
        users.put("user", new User("user", passwordEncoder.encode("user123"), "USER"));
    }
}
