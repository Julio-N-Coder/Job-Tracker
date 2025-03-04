package com.coding_wielder.Job_Tracker.jobs;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coding_wielder.Job_Tracker.configuration.BaseControllerTestUnit;

// EnableAutoConfiguration excludes db
@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class JobControllerTest extends BaseControllerTestUnit {
  private final List<Job> jobs = new ArrayList<>();
  private final RequestJob requestJob = new RequestJob("new title", "new company", "Senior Web Devloper");

  @BeforeEach
  void setup() {
    baseSetup(userId, jobId, jwtUtil);

    Job newJob = new Job(jobId, "fake title", "fake company", "Junior Web Developer", LocalDateTime.now(), userId);
    jobs.add(newJob);
  }

  private void findByIdAndByUserIdMock() {
    when(jobRepository.findByIdAndUserId(jobId, userId)).thenReturn(Optional.of(jobs.get(0)));
  }

  @Test
  void shouldReturnJobList() throws Exception {
    when(jobRepository.findByUserId(userId)).thenReturn(jobs);

    mvc.perform(get("/jobs").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()").value(jobs.size()));
  }

  @Test
  void shouldReturnJob() throws Exception {
    findByIdAndByUserIdMock();

    mvc.perform(get("/job/{id}", jobId).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(jobId.toString()))
        .andExpect(jsonPath("$.jobTitle").value(jobs.get(0).jobTitle()));
  }

  @Test
  void updateJobTest() throws Exception {
    findByIdAndByUserIdMock();

    mvc.perform(get("/job/{id}", jobId)
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestJob)))
        .andExpect(status().isOk());

    when(jobRepository.findByIdAndUserId(jobId, userId)).thenReturn(Optional.empty());
    mvc.perform(get("/job/{id}", jobId).header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestJob)))
        .andExpect(status().isNotFound());
  }
}
