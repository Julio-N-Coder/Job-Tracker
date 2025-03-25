package com.coding_wielder.Job_Tracker.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@SpringBootTest(classes = { ObjectMapper.class })
public class TestTokenLib {
    private static ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper wiredObjectMapper) {
        objectMapper = wiredObjectMapper;
    }

    public void testToken(String token, String token_type, UUID userId) {
        String[] splitToken = token.split("\\.");
        String stringHeader = splitToken[0];
        String stringBody = splitToken[1];

        Header header = base64Decode(stringHeader, Header.class);
        assertEquals(header.alg(), "EdDSA", "Header alg is not EdDSA");

        Body body = base64Decode(stringBody, Body.class);
        assertEquals(body.sub(), userId, "Does not Equal userId");
        if (token_type.equals("refresh_token")) {
            assertEquals(body.token_use(), "refresh_token", "Not a refresh token");
        } else {
            assertEquals(body.token_use(), "token", "Is not a normal token");
        }
    }

    public <T> T base64Decode(String base64String, Class<T> clazz) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            String jsonString = new String(decodedBytes);
            return objectMapper.readValue(jsonString, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode base64 string", e);
        }
    }

    record Header(String alg) {
    }

    record Body(UUID sub, Integer iat, String token_use, Integer exp) {
    }
}
