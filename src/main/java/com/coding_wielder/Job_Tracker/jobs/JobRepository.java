package com.coding_wielder.Job_Tracker.jobs;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends ListCrudRepository<Job, UUID> {

  public Optional<Job> findByIdAndUserId(UUID id, UUID userId);

  @Modifying
  @Query("DELETE FROM jobs WHERE id=:id AND user_id=:userId")
  public int deleteByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

  List<Job> findByUserId(UUID userId);

}
