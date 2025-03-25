package com.coding_wielder.Job_Tracker.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.coding_wielder.Job_Tracker.lib.TestTokenLib;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.JwtException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = { KeyUtil.class, JwtUtil.class, ObjectMapper.class, TestTokenLib.class })
public class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    TestTokenLib testTokenLib;

    private static final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void generateTokenTest() {
        String token = jwtUtil.generateToken(userId);

        testTokenLib.testToken(token, "token", userId);
    }

    @Test
    void generateRefreshTokenTest() {
        String token = jwtUtil.generateRefreshToken(userId);

        testTokenLib.testToken(token, "refresh_token", userId);
    }

    @Test
    void refreshTest() {
        String refresh_token = jwtUtil.generateRefreshToken(userId);
        String token = jwtUtil.refresh(refresh_token);

        testTokenLib.testToken(token, "token", userId);

        // only refresh tokens can refresh tokens
        assertThrows(JwtException.class, () -> {
            jwtUtil.refresh(token);
        });
    }
}
