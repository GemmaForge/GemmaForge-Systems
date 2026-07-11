package com.mlm.backend.Model;

public enum UserRole {

    ENGINEER,  // Can upload code and trigger migrations
    REVIEWER,  // Senior dev who approves/rejects PRs
      // System administrator (Corporate Dashboard access)
}