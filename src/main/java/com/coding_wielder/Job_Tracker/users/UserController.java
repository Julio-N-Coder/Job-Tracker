package com.coding_wielder.Job_Tracker.users;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class UserController {

  private final UserRepository userRepository;

  UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }
  
  @GetMapping("/user/{id}")
  public ResponseEntity<User> getUser(@PathVariable UUID id) {
    Optional<User> userOptional = userRepository.findById(id);
    if (userOptional.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    User user = userOptional.get();
    return ResponseEntity.ok(user);
  }

  @PostMapping("/user")
  public void makeUser(@RequestBody RequestUser requestUser) {
      //TODO: process POST request
  }
}

record RequestUser(
  String username,
  String password
) {}

@Table("users")
record User(
  @Id
  UUID id,
  String username,
  String hashedPassword
) {}