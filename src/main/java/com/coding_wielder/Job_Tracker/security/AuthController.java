package com.coding_wielder.Job_Tracker.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;

import com.coding_wielder.Job_Tracker.lib.Lib;
import com.coding_wielder.Job_Tracker.users.User;
import com.coding_wielder.Job_Tracker.users.UserRepository;

import io.jsonwebtoken.JwtException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

// routes prefixed with /auth/ will not go through jwt security filter
@RestController
public class AuthController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final JdbcClient jdbcClient;
  private final Lib lib;

  public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
      JdbcClient jdbcClient, Lib lib) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.jdbcClient = jdbcClient;
    this.lib = lib;
  }

  private boolean validate(String userName, String password) {
    if (userName == null || userName.length() < 3 || userName.length() > 30) {
      return false;
    }

    if (password == null || password.length() < 2 || password.length() > 100) {
      return false;
    }

    return true;
  }

  @PostMapping("/auth/login")
  public ResponseEntity<?> login(@RequestBody AuthRequest userData) {
    String userName = userData.username();
    String password = userData.password();

    if (!validate(userName, password)) {
      return ResponseEntity.badRequest().body("Invalid Data");
    }

    Optional<User> userOptional = userRepository.findByUsername(userName);
    if (userOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    User user = userOptional.get();
    if (!passwordEncoder.matches(password, user.hashedPassword())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String token = jwtUtil.generateToken(user.id());
    String refresh_token = jwtUtil.generateRefreshToken(user.id());

    return ResponseEntity.ok(new TokenResponse(token, refresh_token));
  }

  @PostMapping("/auth/signup")
  public ResponseEntity<?> signup(@RequestBody AuthRequest userData) {
    String userName = userData.username();
    String password = userData.password();

    if (!validate(userName, password)) {
      return ResponseEntity.badRequest().body("Invalid Data");
    }

    if (userRepository.findByUsername(userName).isPresent()) {
      return ResponseEntity.badRequest().body("Username already taken");
    }

    // store new user
    UUID id = UUID.randomUUID();
    int rowsAffected = jdbcClient
        .sql("INSERT INTO users (id, username, hashed_password) VALUES (:id, :username, :password)")
        .param("id", id)
        .param("username", userName)
        .param("password", passwordEncoder.encode(password))
        .update();

    if (rowsAffected < 1) {
      return ResponseEntity.internalServerError().build();
    }

    String token = jwtUtil.generateToken(id);
    String refresh_token = jwtUtil.generateRefreshToken(id);

    return ResponseEntity.ok(new TokenResponse(token, refresh_token));
  }

  @GetMapping("/token/refresh")
  public ResponseEntity<String> refreshToken() {
    String refresh_token = lib.getToken();
    try {
      String newToken = jwtUtil.refresh(refresh_token);
      return ResponseEntity.ok(newToken);
    } catch (JwtException error) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
    }
  }

}

record AuthRequest(String username, String password) {
}

record TokenResponse(String token, String refresh_token) {
}
