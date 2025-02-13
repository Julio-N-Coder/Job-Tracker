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
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coding_wielder.Job_Tracker.security.JwtUtil;
import com.coding_wielder.Job_Tracker.users.User;
import com.coding_wielder.Job_Tracker.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class}) // don't need db
public class JobControllerTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  JwtUtil jwtUtil;
  
  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  JobRepository jobRepository;
  @MockitoBean
  UserRepository userRepository;
  @MockitoBean
  JdbcClient jdbcClient;

  private final List<Job> jobs = new ArrayList<>();
  private final RequestJob requestJob = new RequestJob("new title", "new company", "Senior Web Devloper");
  private final UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111112");
  private String token;

  @BeforeEach
  void setup() {
    // needed for when userDetailsService calls findById
    when(userRepository.findById(userId)).thenReturn(Optional.of(new User(userId, "username", "fakeHashPassword")));
    token = jwtUtil.generateToken(userId);

    Job newJob = new Job(id, "fake title", "fake company", "Junior Web Developer", LocalDateTime.now(), userId);
    jobs.add(newJob);
  }

  private void findByIdAndByUserIdMock() {
    when(jobRepository.findByIdAndByUserId(id, userId)).thenReturn(Optional.of(jobs.get(0)));
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

    mvc.perform(get("/job/{id}", id).header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(id.toString()))
          .andExpect(jsonPath("$.jobTitle").value(jobs.get(0).jobTitle()));
  }

  @Test
  void updateJobTest() throws Exception {
    findByIdAndByUserIdMock();

    mvc.perform(get("/job/{id}", id).header("Authorization", "Bearer " + token)
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(requestJob)))
        .andExpect(status().isOk());
    
    when(jobRepository.findByIdAndByUserId(id, userId)).thenReturn(Optional.empty());
    mvc.perform(get("/job/{id}", id).header("Authorization", "Bearer " + token)
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(requestJob)))
        .andExpect(status().isNotFound());
  }
}
