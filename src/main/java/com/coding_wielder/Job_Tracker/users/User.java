package com.coding_wielder.Job_Tracker.users;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public record User(
  @Id
  UUID id,
  String username,
  String hashedPassword
) {}
