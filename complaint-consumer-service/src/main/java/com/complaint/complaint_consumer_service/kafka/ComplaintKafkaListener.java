package com.complaint.complaint_consumer_service.kafka;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff; 
import org.springframework.kafka.annotation.RetryableTopic; 


import com.complaint.complaint_consumer_service.DTO.ComplaintEventDTO;
import com.complaint.complaint_consumer_service.domain.model.Complaint;
import com.complaint.complaint_consumer_service.domain.service.ComplaintService;
import com.fasterxml.jackson.databind.ObjectMapper;


@Slf4j
@Component
public class ComplaintKafkaListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ComplaintService complaintService;

    public ComplaintKafkaListener(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }


    // 1. Automatically retries 3 times, then sends to "complaints.created-dlt"
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 2000, multiplier = 2.0),
        autoCreateTopics = "true"
    )
    @KafkaListener(topics = "complaints.created", groupId = "complaint-consumers")
    public void consumeMessage(String message) {
        ComplaintEventDTO complaintEvent = null;
        try {
         
            // Deserialize JSON message to ComplaintEventDTO object
            complaintEvent = objectMapper.readValue(message, ComplaintEventDTO.class);
         
            Complaint complaint = complaintEvent.getPayload();
         
            complaintService.saveComplaint(complaint);

            log.info("Complaint processed for user: {} and message: {}", complaint.getUser(), complaint.getMessage());
        
        } catch (Exception e) {
            log.error("Error processing message. Raw message was: {}", message, e);


            // Now you can safely check if complaintEvent was successfully parsed
            if (complaintEvent != null) {
                log.error("Failed specifically at business logic for user: {}", complaintEvent.getPayload().getUser());
                // saveToErrorLog(complaintEvent.getPayload(), e);
            }
          
          throw e;
        }
    }

    @DltHandler
    public void handleFailedMessage(String message) {
        log.info(" Message moved to DLT: " + message);
    }

    // 2. This method catches messages that failed all 3 retry attempts
    // @DltHandler
    // public void handleDlt(ComplaintEventDTO event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    //     log.error("Event sent to Dead Letter Topic ({}): {}", topic, event);
    //     // Here you can save to a special 'failed_complaints' table if you still want to
    // }

    private void saveToErrorLog(Complaint complaint, Exception e) {
        // write to file /send email /store in another DB
    }
}
