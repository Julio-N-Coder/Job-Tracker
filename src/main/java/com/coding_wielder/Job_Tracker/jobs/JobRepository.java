package com.coding_wielder.Job_Tracker.jobs;

import org.springframework.data.repository.ListCrudRepository;

public interface JobRepository extends ListCrudRepository<Job, String> {
}
