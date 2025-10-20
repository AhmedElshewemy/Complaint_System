package com.complaint.complaint_consumer_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.complaint.complaint_consumer_service.domain.model.Complaint;
import com.complaint.complaint_consumer_service.domain.service.ComplaintService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ComplaintKafkaListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ComplaintService complaintService;

    public ComplaintKafkaListener(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @KafkaListener(topics = "complaints.created", groupId = "complaint-consumers")
    public void consumeMessage(String message) {
        try {
            // Deserialize JSON message to Complaint object
            Complaint complaint = objectMapper.readValue(message, Complaint.class);
            // Save the complaint using the service
            complaintService.saveComplaint(complaint);
            System.out.println("✅ Complaint processed for user: " + complaint.getUser());
        } catch (Exception e) {
            System.err.println("❌ Error processing message: " + e.getMessage());

        }
    }
}
