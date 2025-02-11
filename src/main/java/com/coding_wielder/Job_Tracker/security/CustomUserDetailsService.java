package com.coding_wielder.Job_Tracker.security;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.coding_wielder.Job_Tracker.users.User;
import com.coding_wielder.Job_Tracker.users.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  
  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
      this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      return userRepository.findByUsername(username).map(this::mapToUserDetails)
              .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }

  public CustomUserDetails loadUserById(UUID userId) {
      return userRepository.findById(userId)
        .map(this::mapToUserDetails)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
  }

  private CustomUserDetails mapToUserDetails(User user) {
  return new CustomUserDetails(
          user.id(),
          user.username(),
          user.hashedPassword(),
          List.of(new SimpleGrantedAuthority("USER"))
  );
  }

}
