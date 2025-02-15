package com.coding_wielder.Job_Tracker.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.coding_wielder.Job_Tracker.configuration.BaseControllerTestUnit;
import com.coding_wielder.Job_Tracker.users.User;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class AuthControllerTest extends BaseControllerTestUnit {

  private final String username = "username";
  private final String password = "password";
  private final User user = new User(userId, username, password);
  private final AuthRequest userData = new AuthRequest(username, password);
  private final String token = "fakeToken";

  @MockitoBean
  public JwtUtil jwtUtil;
  @MockitoBean
  public PasswordEncoder passwordEncoder;
  @MockitoBean
  StatementSpec statementSpec;

  @BeforeEach
  private void beforeEach() {
    when(jwtUtil.generateToken(any(UUID.class))).thenReturn(token);
  }

  private MockHttpServletRequestBuilder postMethod(String path) throws Exception {
    return post(path)
        .servletPath(path) // need to explicitly set path for filter to work
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userData));
  }

  @Test
  void loginTest() throws Exception {
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(password, password)).thenReturn(true);

    String responseBody = mvc.perform(postMethod("/auth/login"))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    assertEquals(responseBody, token);
  }

  @Test
  void signupTest() throws Exception {
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    when(jdbcClient.sql(anyString())).thenReturn(statementSpec);
    when(statementSpec.param(anyString(), any())).thenReturn(statementSpec);
    when(statementSpec.update()).thenReturn(1); // Simulate 1 row affected

    when(passwordEncoder.encode(password)).thenReturn(password);

    String responseBody = mvc.perform(postMethod("/auth/signup"))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    assertEquals(responseBody, token);
  }
}
