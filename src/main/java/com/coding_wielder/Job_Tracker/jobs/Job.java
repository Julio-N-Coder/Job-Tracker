package com.coding_wielder.Job_Tracker.jobs;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("jobs")
public record Job(
  @Id
  UUID id,
  String jobTitle,
  String company,
  String status,
  LocalDateTime appliedDate,
  UUID userId
) {
}
