package com.mlm.backend.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "migration_errors")
public class MigrationError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "error_code", unique = true, nullable = false)
    private String errorCode; // e.g., "HIP-1042" or a generic identifier

    @Column(name = "error_snippet", nullable = false)
    private String errorSnippet; // A common string found in the raw compiler output to match against

    @Column(name = "human_readable_explanation", columnDefinition = "TEXT", nullable = false)
    private String humanReadableExplanation;

    @Column(name = "suggested_fix", columnDefinition = "TEXT", nullable = false)
    private String suggestedFix;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorSnippet() { return errorSnippet; }
    public void setErrorSnippet(String errorSnippet) { this.errorSnippet = errorSnippet; }

    public String getHumanReadableExplanation() { return humanReadableExplanation; }
    public void setHumanReadableExplanation(String humanReadableExplanation) { this.humanReadableExplanation = humanReadableExplanation; }

    public String getSuggestedFix() { return suggestedFix; }
    public void setSuggestedFix(String suggestedFix) { this.suggestedFix = suggestedFix; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}