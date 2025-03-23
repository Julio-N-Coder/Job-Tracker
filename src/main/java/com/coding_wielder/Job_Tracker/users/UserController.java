package com.coding_wielder.Job_Tracker.users;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coding_wielder.Job_Tracker.lib.Lib;

@RestController
@RequestMapping("/user")
public class UserController {

  private final UserRepository userRepository;
  private final Lib lib;

  UserController(UserRepository userRepository, Lib lib) {
    this.userRepository = userRepository;
    this.lib = lib;
  }

  @GetMapping("")
  public ResponseEntity<ResponseUser> getUser() {
    Optional<User> userOptional = userRepository.findById(lib.getPrinciple());
    if (userOptional.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    User user = userOptional.get();
    return ResponseEntity.ok(new ResponseUser(user.id(), user.username()));
  }

  @DeleteMapping("")
  public ResponseEntity<Void> deleteUserAccount() {
    // check token type once refresh token is added
    int rowsAffectd = userRepository.deleteByIdWithFeedback(lib.getPrinciple());
    if (rowsAffectd < 1) {
      ResponseEntity.internalServerError();
    }

    return ResponseEntity.ok().build();
  }

}

record RequestUser(
    String username,
    String password) {
}

record ResponseUser(UUID id, String username) {
}
