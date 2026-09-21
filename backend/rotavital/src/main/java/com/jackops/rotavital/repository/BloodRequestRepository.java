package com.jackops.rotavital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jackops.rotavital.model.BloodRequest;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
}
