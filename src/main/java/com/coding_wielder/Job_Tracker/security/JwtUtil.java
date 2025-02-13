package com.coding_wielder.Job_Tracker.security;

import java.security.KeyPair;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureAlgorithm;

// Later implement token generation for refresh token and normal token
@Component
public class JwtUtil {
  
  private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour
  // Temporary for development for the time (Make sure to use actual keys later)
  // (first check if jjwt supports it) switch to Ed25519 algorithm. builder: Jwks.CRV.Ed25519.keyPair()
  SignatureAlgorithm alg = Jwts.SIG.ES256;
  private final KeyPair keyPair = alg.keyPair().build();
  // private final KeyPair keyPair = Jwks.CRV.Ed25519.keyPair().build();

  public String generateToken(UUID id) {
    return Jwts.builder()
      .subject(id.toString())
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
      .signWith(keyPair.getPrivate(), alg)
      .compact();
  }

  public UUID validateTokenAndReturnSubject(String token) throws JwtException {
    Claims jwtClaims = Jwts.parser()
      .verifyWith(keyPair.getPublic())
      .build()
      .parseSignedClaims(token)
      .getPayload();

    Date expiration = jwtClaims.getExpiration();
    if (expiration.before(new Date())) {
      throw new JwtException("Expired Token");
    }

    return UUID.fromString(jwtClaims.getSubject());
  }
}
