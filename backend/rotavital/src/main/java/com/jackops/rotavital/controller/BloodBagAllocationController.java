package com.jackops.rotavital.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jackops.rotavital.dto.BloodBagAllocationResponse;
import com.jackops.rotavital.service.BloodBagAllocationService;

@RestController
@RequestMapping("/api/allocations")
public class BloodBagAllocationController {

    private final BloodBagAllocationService bloodBagAllocationService;

    public BloodBagAllocationController(BloodBagAllocationService bloodBagAllocationService) {
        this.bloodBagAllocationService = bloodBagAllocationService;
    }

    @PostMapping("/requests/{requestId}/allocate")
    public ResponseEntity<BloodBagAllocationResponse> allocate(@PathVariable Long requestId) {
        try {
            BloodBagAllocationResponse response = bloodBagAllocationService.allocateCompatibleBag(requestId);
            if (response.isAllocated()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.accepted().body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new BloodBagAllocationResponse(null, false, ex.getMessage()));
        }
    }
}
