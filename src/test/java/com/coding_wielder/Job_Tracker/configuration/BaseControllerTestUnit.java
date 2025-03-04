package com.coding_wielder.Job_Tracker.configuration;

import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.coding_wielder.Job_Tracker.jobs.JobRepository;
import com.coding_wielder.Job_Tracker.security.JwtUtil;
import com.coding_wielder.Job_Tracker.users.User;
import com.coding_wielder.Job_Tracker.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BaseControllerTestUnit {
  @Autowired
  public MockMvc mvc;

  @Autowired
  public JwtUtil jwtUtil;

  @Autowired
  public ObjectMapper objectMapper;

  @MockitoBean
  public JobRepository jobRepository;
  @MockitoBean
  public UserRepository userRepository;
  @MockitoBean
  public JdbcClient jdbcClient;

  protected final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111112");
  protected final UUID jobId = UUID.fromString("11111111-1111-1111-1111-111111111111");
  protected String token;

  protected void baseSetup(UUID userId, UUID jobId, JwtUtil jwtUtil) {
    // needed for when userDetailsService calls findById
    when(userRepository.findById(userId)).thenReturn(Optional.of(new User(userId, "username", "fakeHashPassword")));
    token = jwtUtil.generateToken(userId);
  }
}
