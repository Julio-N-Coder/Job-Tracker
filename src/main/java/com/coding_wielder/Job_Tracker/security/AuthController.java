package com.coding_wielder.Job_Tracker.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coding_wielder.Job_Tracker.users.User;
import com.coding_wielder.Job_Tracker.users.UserRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/auth")
public class AuthController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final JdbcClient jdbcClient;
  
  public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, JdbcClient jdbcClient) {
      this.userRepository = userRepository;
      this.passwordEncoder = passwordEncoder;
      this.jwtUtil = jwtUtil;
      this.jdbcClient = jdbcClient;
  }

  private boolean validate(String userName, String password) {
    if (userName == null || userName.length() < 3 || userName.length() > 30) {
      return false;
    }

    if (password == null || password.length() < 2) {
      return false;
    }

    return true;
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody AuthRequest userData) {
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
    return ResponseEntity.ok(token);
  }
  

  @PostMapping("/signup")
  public ResponseEntity<String> signup(@RequestBody AuthRequest userData) {
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
    int rowsAffected = jdbcClient.sql("INSERT INTO users (id, username, hashed_password) VALUES (:id, :username, :password)")
      .param("id", id)
      .param("username", userName)
      .param("password", passwordEncoder.encode(password))
      .update();
    
    if (rowsAffected < 1) {
      return ResponseEntity.internalServerError().build();
    }

    String token = jwtUtil.generateToken(id);
    
    return ResponseEntity.ok(token);
  }

  // add refresh token method here
}

record AuthRequest(String username, String password) {}
