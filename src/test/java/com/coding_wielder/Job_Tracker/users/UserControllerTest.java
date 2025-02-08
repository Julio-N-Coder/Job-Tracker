package com.coding_wielder.Job_Tracker.users;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
public class UserControllerTest {
  
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;
  
  @MockitoBean
  private UserRepository userRepository;

  private final UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private User oneUser = new User(id, "fakeUserName", "Fake Hashed Password");

  @Test
  void getUserTest() throws Exception {
    String oneUserJson = objectMapper.writeValueAsString(oneUser);
    when(userRepository.findById(id)).thenReturn(Optional.of(oneUser));

    mockMvc.perform(get("/user/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(oneUserJson)
      ).andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(id.toString()));
    
    when(userRepository.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(get("/user/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(oneUserJson)
      ).andExpect(status().isNotFound());
  }

}
