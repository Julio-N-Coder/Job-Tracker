package com.coding_wielder.Job_Tracker.jobs;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.bind.annotation.RestController;

import com.coding_wielder.Job_Tracker.lib.Lib;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
public class JobController {

  private final JobRepository jobRepository;
  private final Lib lib;

  JobController(JobRepository jobRepository, Lib lib) {
    this.jobRepository = jobRepository;
    this.lib = lib;
  }

  private boolean jobSanitation(RequestJob requestJob) {
    String company = requestJob.company();
    String jobTitle = requestJob.jobTitle();
    String status = requestJob.status();

    if (
      company == null || jobTitle == null || status == null || 
      company.length() < 1 || jobTitle.length() < 1 || status.length() < 1 || 
      company.length() > 255 || jobTitle.length() > 255 || status.length() > 255
      ) {
      return false;
    }

    return true;
  }

  @GetMapping("/jobs")
  public List<Job> getJobs() {
    // add pagination later with just jdbcClient using raw sql
    return jobRepository.findByUserId(lib.getPrinciple());
  }

  @GetMapping("/job/{id}")
  public ResponseEntity<Job> getJobById(@PathVariable UUID id) {
    Optional<Job> job = jobRepository.findByIdAndUserId(id, lib.getPrinciple());
    if (job.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    return ResponseEntity.ok(job.get());
  }
  

  @PostMapping("/job")
  public ResponseEntity<Void> addNewJob(@RequestBody RequestJob requestJob) {
    if (!jobSanitation(requestJob)) {
      return ResponseEntity.badRequest().build();
    }

    Job newJob = new Job(null,
                          requestJob.jobTitle(),
                          requestJob.company(),
                          requestJob.status(),
                          LocalDateTime.now(),
                          lib.getPrinciple()
                        );
    jobRepository.save(newJob);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/job/{id}")
  public ResponseEntity<Void> updateJob(@PathVariable UUID id, @RequestBody RequestJob requestJob) {
    if (!jobSanitation(requestJob)) {
      return ResponseEntity.badRequest().build();
    }

    Optional<Job> oldJobOptional = jobRepository.findByIdAndUserId(id, lib.getPrinciple());
    if (oldJobOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    Job oldJob = oldJobOptional.get();
    Job newJob = new Job(
      id, 
      requestJob.jobTitle(), 
      requestJob.company(), 
      requestJob.status(), 
      oldJob.appliedDate(),
      lib.getPrinciple()
    );

    jobRepository.save(newJob);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/job/{id}")
  public ResponseEntity<Void> deleteJob(@PathVariable UUID id) {
    int rowsAffected = jobRepository.deleteByIdAndUserId(id, lib.getPrinciple());
    System.out.println("Rows Affected: " + rowsAffected);
    if (rowsAffected < 1) {
      return ResponseEntity.badRequest().build();
    }

    return ResponseEntity.ok().build();
  }
}

record RequestJob(
  String jobTitle,
  String company,
  String status
) {}
