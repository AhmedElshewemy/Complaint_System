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



    @KafkaListener(topics = "complaints.created", groupId = "complaint-consumers")
    public void consumeMessage(ComplaintEventDTO complaintEvent) throws Exception{

        try {
         
            Complaint complaint = complaintEvent.getPayload();
         
        
        if (complaint.getUser() == null) {
            throw new IllegalArgumentException("User is required");
        }

            complaintService.saveComplaint(complaint);
              log.info(
            "Complaint saved. id={}, user={} and message: {}",
            complaint.getId(),
            complaint.getUser(),
            complaint.getMessage()
        );
        
        } catch (Exception e) {
            log.error("Error processing message. Raw message was: {}", complaintEvent, e);


            // Now you can safely check if complaintEvent was successfully parsed
            if (complaintEvent != null) {
                log.error("Failed specifically at business logic for user: {}", complaintEvent.getPayload().getUser());
               
            }
          
          throw e;
        }
    }

       @KafkaListener(
        topics = "complaints.created.DLQ",
        groupId = "complaint-dlq-consumers"
    )
    public void consumeDlq(
            ComplaintEventDTO event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String error
    ) {
        log.error(
            "DLQ message received from {}. Error={}. Payload={}",
            topic,
            error,
            event
        );
        }


}
