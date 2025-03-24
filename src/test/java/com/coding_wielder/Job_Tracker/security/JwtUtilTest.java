package com.coding_wielder.Job_Tracker.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.JwtException;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = { KeyUtil.class, JwtUtil.class, ObjectMapper.class })
public class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    record Header(String alg) {
    }

    record Body(UUID sub, Integer iat, String token_use, Integer exp) {
    }

    @Test
    void generateTokenTest() {
        String token = jwtUtil.generateToken(userId);

        testToken(token, "token");
    }

    @Test
    void generateRefreshTokenTest() {
        String token = jwtUtil.generateRefreshToken(userId);

        testToken(token, "refresh_token");
    }

    @Test
    void refreshTest() {
        String refresh_token = jwtUtil.generateRefreshToken(userId);
        String token = jwtUtil.refresh(refresh_token);

        testToken(token, "token");

        // only refresh tokens can refresh tokens
        assertThrows(JwtException.class, () -> {
            jwtUtil.refresh(token);
        });
    }

    private void testToken(String token, String token_type) {
        String[] splitToken = token.split("\\.");
        String stringHeader = splitToken[0];
        String stringBody = splitToken[1];

        Header header = base64Decode(stringHeader, Header.class);
        assertEquals(header.alg(), "EdDSA", "Header alg is not EdDSA");

        Body body = base64Decode(stringBody, Body.class);
        assertEquals(body.sub(), userId, "Does not Equal userId");
        if (token_type == "refresh_token") {
            assertEquals(body.token_use(), "refresh_token", "Not a refresh token");
        } else {
            assertEquals(body.token_use(), "token", "Is not a normal token");
        }
    }

    private <T> T base64Decode(String base64String, Class<T> clazz) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            String jsonString = new String(decodedBytes);
            return objectMapper.readValue(jsonString, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode base64 string", e);
        }
    }
}
