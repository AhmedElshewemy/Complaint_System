package com.complaint.complaint_consumer_service.domain.service;

import org.springframework.stereotype.Service;

import com.complaint.complaint_consumer_service.domain.model.Complaint;
import com.complaint.complaint_consumer_service.domain.repository.ComplaintRepository;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    // Constructor-based dependency injection 
    public ComplaintService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    public void saveComplaint(Complaint complaint) {
        //
        complaintRepository.save(complaint);
        System.out.println(" Complaint saved: " + complaint.getUser());
    }

    public void deleteComplaint(Long complaintId) {
        complaintRepository.deleteById(complaintId);
        System.out.println(" Complaint deleted with ID: " + complaintId);
    }

    


}
