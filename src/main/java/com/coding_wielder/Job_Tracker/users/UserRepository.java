package com.coding_wielder.Job_Tracker.users;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;


public interface UserRepository extends ListCrudRepository<User, UUID> {

  public Optional<User> findByUsername(String username);

}
