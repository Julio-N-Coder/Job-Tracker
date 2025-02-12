package com.coding_wielder.Job_Tracker.jobs;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

// having problem with this loading auth stuff as well
@WebMvcTest(JobController.class)
public class JobControllerTest {
  @Autowired
  MockMvc mvc;
  
  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  JobRepository jobRepository;

  private final List<Job> jobs = new ArrayList<>();
  private final RequestJob requestJob = new RequestJob("new title", "new company", "Senior Web Devloper");
  private final UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111112");

  @BeforeEach
  void setup() {
    Job newJob = new Job(id, "fake title", "fake company", "Junior Web Developer", LocalDateTime.now(), userId);
    jobs.add(newJob);
  }

  private void findByIdMock() {
    when(jobRepository.findById(id)).thenReturn(Optional.of(jobs.get(0)));
  }

  @Test
  void shouldReturnJobList() throws Exception {
    when(jobRepository.findAll()).thenReturn(jobs);

    mvc.perform(get("/jobs"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.size()").value(jobs.size()));
  }
  
  @Test
  void shouldReturnJob() throws Exception {
    findByIdMock();

    mvc.perform(get("/job/{id}", id))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(id.toString()))
          .andExpect(jsonPath("$.jobTitle").value(jobs.get(0).jobTitle()));
  }

  @Test
  void updateJobTest() throws Exception {
    findByIdMock();

    mvc.perform(get("/job/{id}", id)
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(requestJob)))
        .andExpect(status().isOk());
    
    when(jobRepository.findById(id)).thenReturn(Optional.empty());
    mvc.perform(get("/job/{id}", id)
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(requestJob)))
        .andExpect(status().isNotFound());
  }
}
