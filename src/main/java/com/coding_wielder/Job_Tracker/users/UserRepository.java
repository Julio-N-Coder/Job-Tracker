package com.coding_wielder.Job_Tracker.users;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;


public interface UserRepository extends ListCrudRepository<User, UUID> {

  public Optional<User> findByUsername(String username);

  @Modifying
  @Query("DELETE FROM users WHERE id = :id")
  int deleteByIdWithFeedback(UUID id);

}
