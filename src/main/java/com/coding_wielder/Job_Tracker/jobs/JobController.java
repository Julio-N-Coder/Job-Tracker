package com.coding_wielder.Job_Tracker.jobs;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
public class JobController {

  private final JobRepository jobRepository;

  JobController(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @GetMapping("/jobs")
  public List<Job> getJobs() {
      // add pagination later with just jdbcClient using raw sql
      // or maybe with jdbcTemplate
      return jobRepository.findAll();
  }

  @GetMapping("/job/{id}")
  public ResponseEntity<Job> getJobById(@RequestParam UUID id) {
    Optional<Job> job = jobRepository.findById(id);
    if (job.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    return ResponseEntity.ok(job.get());
  }
  

  @PostMapping("/job")
  public void addNewJob(@RequestBody RequestJob requestJob) {
    // sanatize input and return ResponseEntity. make method for it RequuestJob sanitation
      Job newJob = new Job(null,
                            requestJob.jobTitle(),
                            requestJob.company(),
                            requestJob.status(),
                            LocalDateTime.now()
                          );
      jobRepository.save(newJob);
  }

  @PutMapping("/job/{id}")
  public ResponseEntity<Void> updateJob(@PathVariable UUID id, @RequestBody RequestJob requestJob) {
      // sanatize input
      Optional<Job> oldJobOptional = jobRepository.findById(id);
      if (oldJobOptional.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
      Job oldJob = oldJobOptional.get();

      jobRepository.save(new Job(id, requestJob.jobTitle(), requestJob.company(), requestJob.status(), oldJob.appliedDate()));
      return ResponseEntity.ok().build();
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/job/{id}")
  public void deleteJob(@PathVariable UUID id) {
    jobRepository.deleteById(id);
  }
}

record RequestJob(
  String jobTitle,
  String company,
  String status
) {}

@Table("jobs")
record Job(
  @Id
  UUID id,
  String jobTitle,
  String company,
  String status,
  LocalDateTime appliedDate
) {
}
