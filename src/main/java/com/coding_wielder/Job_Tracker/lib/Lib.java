package com.coding_wielder.Job_Tracker.lib;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.coding_wielder.Job_Tracker.security.CustomUserDetails;

@Component
public class Lib {
  public UUID getPrinciple() {
    return ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
  }
}
