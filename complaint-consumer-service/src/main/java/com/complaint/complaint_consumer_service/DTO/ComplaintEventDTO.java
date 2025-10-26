package com.complaint.complaint_consumer_service.DTO;
import com.complaint.complaint_consumer_service.domain.model.Complaint;


public class ComplaintEventDTO {

    private String eventType;
    private int eventVersion;
    private String timestamp;
    private Complaint payload;

    // Getters and setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public int getEventVersion() { return eventVersion; }
    public void setEventVersion(int eventVersion) { this.eventVersion = eventVersion; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public Complaint getPayload() { return payload; }
    public void setPayload(Complaint payload) { this.payload = payload; }
    
}
