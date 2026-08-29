package com.sangiya.apisec.auth;

import jakarta.validation.constraints.NotBlank;

public class ClientCredentialsRequest {

    @NotBlank(message = "clientId is required")
    private String clientId;

    @NotBlank(message = "clientSecret is required")
    private String clientSecret;

    public ClientCredentialsRequest() {
    }

    public ClientCredentialsRequest(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
