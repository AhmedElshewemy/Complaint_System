package com.complaint.complaint_consumer_service.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.complaint.complaint_consumer_service.domain.model.Complaint;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    
}
