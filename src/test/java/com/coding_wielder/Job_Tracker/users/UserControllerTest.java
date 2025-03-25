package com.coding_wielder.Job_Tracker.users;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.coding_wielder.Job_Tracker.configuration.BaseControllerTestUnit;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class UserControllerTest extends BaseControllerTestUnit {

  private User oneUser = new User(userId, "fakeUserName", "Fake Hashed Password");

  @BeforeEach
  void temp() {
    token = jwtUtil.generateToken(userId);
  }

  @Test
  void getUserTest() throws Exception {
    String oneUserJson = objectMapper.writeValueAsString(oneUser);
    when(userRepository.findById(userId)).thenReturn(Optional.of(oneUser));

    mvc.perform(get("/user")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(oneUserJson)).andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()));

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class, () -> {
      mvc.perform(get("/user")
          .header("Authorization", "Bearer " + token)
          .contentType(MediaType.APPLICATION_JSON)
          .content(oneUserJson))
          .andExpect(status().isNotFound());
    });
  }

}
