package com.sangiya.apisec.transfer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

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
class TransferValidationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void validTransfer_returns201() throws Exception {
        String token = obtainToken();

        mvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson("250.00", "USR-12345", "rent")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recipient").value("USR-12345"))
                .andExpect(jsonPath("$.amount").value(250.00))
                .andExpect(jsonPath("$.from").isNotEmpty());
    }

    @Test
    void negativeAmount_returns400() throws Exception {
        String token = obtainToken();

        mvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson("-5.00", "USR-12345", "tax")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("amount"));
    }

    @Test
    void zeroAmount_returns400() throws Exception {
        String token = obtainToken();

        mvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson("0.00", "USR-12345", "noop")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("amount"));
    }

    @Test
    void oversizedAmount_returns400() throws Exception {
        String token = obtainToken();

        mvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson("999999.00", "USR-12345", "overflow")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("amount"));
    }

    @Test
    void badRecipientFormat_returns400() throws Exception {
        String token = obtainToken();

        mvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson("10.00", "not-an-account", "bad-recipient")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("recipient"));
    }

    @Test
    void recipientWithWrongPattern_returns400() throws Exception {
        String token = obtainToken();

        // matches size but not the pattern (lowercase / wrong digits)
        mvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson("10.00", "USR-12A45", "bad-pattern")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("recipient"));
    }

    @Test
    void missingAmount_returns400() throws Exception {
        String token = obtainToken();

        ObjectNode body = objectMapper.createObjectNode()
                .put("recipient", "USR-12345");

        mvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"));
    }

    @Test
    void unauthenticatedTransfer_returns401() throws Exception {
        mvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson("10.00", "USR-12345", "anon")))
                .andExpect(status().isUnauthorized());
    }

    private String obtainToken() throws Exception {
        ObjectNode body = objectMapper.createObjectNode()
                .put("username", "user")
                .put("password", "user123");

        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    private byte[] transferJson(String amount, String recipient, String reference) {
        ObjectNode body = objectMapper.createObjectNode()
                .put("amount", new BigDecimal(amount))
                .put("recipient", recipient)
                .put("reference", reference);
        return body.toString().getBytes();
    }
}
