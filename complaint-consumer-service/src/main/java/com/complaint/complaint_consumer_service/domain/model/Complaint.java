package com.complaint.complaint_consumer_service.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;          // Auto-generated DB ID
    @Column(name = "username")
    private String user;      // User who submitted the complaint
    private String category;  // Complaint type (e.g., "Service Delay")
    private String message;   // Complaint content
    private String created;   // Timestamp (string for simplicity)

      // Required no-args constructor for JPA
    public Complaint() {}

    // All-args constructor for convenience
    public Complaint(String user, String category, String message, String created) {
        this.user = user;
        this.category = category;
        this.message = message;
        this.created = created;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCreated() { return created; }
    public void setCreated(String created) { this.created = created; }
}
