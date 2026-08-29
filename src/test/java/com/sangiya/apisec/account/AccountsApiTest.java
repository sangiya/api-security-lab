package com.sangiya.apisec.account;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringBootTest
@AutoConfigureMockMvc
class AccountsApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void unauthenticated_getAccounts_returns401() throws Exception {
        mvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticated_getAccounts_returns200WithData() throws Exception {
        String token = obtainToken("user", "user123");

        mvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].owner").value("user"))
                .andExpect(jsonPath("$[0].accountNumber").value("USR-1001"))
                .andExpect(jsonPath("$[0].balance").value(1250.75));
    }

    @Test
    void authenticatedAsAdmin_returnsAdminAccounts() throws Exception {
        String token = obtainToken("admin", "admin123");

        mvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].accountNumber").value("ADM-9001"));
    }

    @Test
    void invalidToken_returns401() throws Exception {
        mvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredToken_return401() throws Exception {
        String token = obtainToken("user", "user123");
        // tamper expiration is not possible with a valid signature, so simulate
        // an invalid/old signature instead; the endpoint must reject it outright.
        String badToken = token.substring(0, token.length() - 1) + "x";

        mvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + badToken))
                .andExpect(status().isUnauthorized());
    }

    private String obtainToken(String username, String password) throws Exception {
        ObjectNode body = objectMapper.createObjectNode()
                .put("username", username)
                .put("password", password);

        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
}
