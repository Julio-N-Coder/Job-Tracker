package com.coding_wielder.Job_Tracker.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {
  
  private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour
  KeyUtil keyUtil;

  JwtUtil(KeyUtil keyUtil) {
    this.keyUtil = keyUtil;
  }

  public String generateToken(UUID id) {
    return Jwts.builder()
      .subject(id.toString())
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
      .signWith(keyUtil.getPrivateKey(), keyUtil.getAlg())
      .compact();
  }

  public UUID validateTokenAndReturnSubject(String token) throws JwtException {
    Claims jwtClaims = Jwts.parser()
      .verifyWith(keyUtil.getPublicKey())
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

@Component
class KeyUtil {
  public String algString = "Ed25519";
  private SignatureAlgorithm alg = Jwts.SIG.EdDSA;
  private PublicKey publicKey;
  private PrivateKey privateKey;

  @Value("${key.file.path}")
  private String keyFilePath;

  @PostConstruct
  public void init() throws Exception {
    this.publicKey = loadPublicKey(getKeyPath("public.pem"));
    this.privateKey = loadPrivateKey(getKeyPath("private.pem"));
  }

  private PublicKey loadPublicKey(Path publicKeyPath) throws Exception {
    byte[] decodedKey = keyPathToByteArr(publicKeyPath, "PUBLIC");
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
    KeyFactory keyFactory = KeyFactory.getInstance(algString);
    return keyFactory.generatePublic(keySpec);
  }
  
  private PrivateKey loadPrivateKey(Path privateKeyPath) throws Exception {
    byte[] decodedKey = keyPathToByteArr(privateKeyPath, "PRIVATE");
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
    KeyFactory keyFactory = KeyFactory.getInstance(algString);
    return keyFactory.generatePrivate(keySpec);
  }
  
  private Path getKeyPath(String publicOrPrivate) {
    Path keyPath = Path.of(keyFilePath);

    if (keyPath.isAbsolute()) {
      return keyPath;
    }

    return Paths.get(System.getProperty("user.dir"), keyFilePath.toString(), publicOrPrivate);
  }

  private byte[] keyPathToByteArr(Path keyPath, String pubblicOrPrivate) throws IOException {
    byte[] keyBytes = Files.readAllBytes(keyPath);

    String keyContent = new String(keyBytes)
      .replaceAll("-----BEGIN "+ pubblicOrPrivate + " KEY-----", "")
      .replaceAll("-----END "+ pubblicOrPrivate + " KEY-----", "")
      .replaceAll("\\s", "");

    return Base64.getDecoder().decode(keyContent);
  }

  public SignatureAlgorithm getAlg() {
    return alg;
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }
}