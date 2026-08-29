package com.sangiya.apisec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Disable default security bootstrapping; the SecurityConfig chain fully controls access.
@SpringBootApplication
public class ApiSecurityLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiSecurityLabApplication.class, args);
    }
}
