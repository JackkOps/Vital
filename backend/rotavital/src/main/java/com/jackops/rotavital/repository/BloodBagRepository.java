package com.jackops.rotavital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jackops.rotavital.model.BloodBag;
import com.jackops.rotavital.model.BloodBagStatus;

@Repository
public interface BloodBagRepository extends JpaRepository<BloodBag, Long> {
    List<BloodBag> findByStatus(BloodBagStatus status);
}
