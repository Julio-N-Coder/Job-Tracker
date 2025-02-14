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
import org.springframework.web.bind.annotation.ResponseStatus;
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

  @GetMapping("/jobs")
  public List<Job> getJobs() {
    // add pagination later with just jdbcClient using raw sql
    return jobRepository.findByUserId(lib.getPrinciple());
  }

  @GetMapping("/job/{id}")
  public ResponseEntity<Job> getJobById(@PathVariable UUID id) {
    Optional<Job> job = jobRepository.findByIdAndByUserId(id, lib.getPrinciple());
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
                            LocalDateTime.now(),
                            lib.getPrinciple()
                          );
      jobRepository.save(newJob);
  }

  @PutMapping("/job/{id}")
  public ResponseEntity<Void> updateJob(@PathVariable UUID id, @RequestBody RequestJob requestJob) {
      // sanatize input
      Optional<Job> oldJobOptional = jobRepository.findByIdAndByUserId(id, lib.getPrinciple());
      if (oldJobOptional.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
      Job oldJob = oldJobOptional.get();

      jobRepository.save(new Job(
        id, 
        requestJob.jobTitle(), 
        requestJob.company(), 
        requestJob.status(), 
        oldJob.appliedDate(),
        lib.getPrinciple()
      ));
      return ResponseEntity.ok().build();
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/job/{id}")
  public void deleteJob(@PathVariable UUID id) {
    jobRepository.deleteByIdAndByUserId(id, lib.getPrinciple());
  }
}

record RequestJob(
  String jobTitle,
  String company,
  String status
) {}
