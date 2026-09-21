package com.jackops.rotavital.dto;

public class BloodBagAllocationResponse {
    private Long bagId;
    private boolean allocated;
    private String message;

    public BloodBagAllocationResponse() {
    }

    public BloodBagAllocationResponse(Long bagId, boolean allocated, String message) {
        this.bagId = bagId;
        this.allocated = allocated;
        this.message = message;
    }

    public Long getBagId() {
        return bagId;
    }

    public void setBagId(Long bagId) {
        this.bagId = bagId;
    }

    public boolean isAllocated() {
        return allocated;
    }

    public void setAllocated(boolean allocated) {
        this.allocated = allocated;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
