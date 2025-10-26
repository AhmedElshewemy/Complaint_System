package com.complaint.complaint_consumer_service.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;


//@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class Complaint {
    @Id
//    @Column(nullable = false, updatable = false)
    private String id;          // Auto-generated DB ID
    @Column(name = "username")
    private String user;      // User who submitted the complaint
    private String category;  // Complaint type (e.g., "Service Delay")
    private String message;   // Complaint content
    private String status;   // Complaint status (e.g., "Pending", "Resolved")
    private String created;   // Timestamp (string for simplicity)

      // Required no-args constructor for JPA
    public Complaint() {}

    // All-args constructor for convenience
    public Complaint(String user, String category, String message, String status, String created) {
        this.user = user;
        this.category = category;
        this.message = message;
        this.status = status;
        this.created = created;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreated() { return created; }
    public void setCreated(String created) { this.created = created; }
}
