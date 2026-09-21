package com.jackops.rotavital.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.jackops.rotavital.dto.BloodBagAllocationResponse;
import com.jackops.rotavital.model.BloodBag;
import com.jackops.rotavital.model.BloodBagStatus;
import com.jackops.rotavital.model.BloodRequest;
import com.jackops.rotavital.model.BloodType;
import com.jackops.rotavital.model.ComponentType;
import com.jackops.rotavital.model.RhFactor;
import com.jackops.rotavital.repository.BloodBagRepository;
import com.jackops.rotavital.repository.BloodRequestRepository;

@SpringBootTest
@Transactional
class BloodBagAllocationServiceTest {

    @Autowired
    private BloodBagAllocationService bloodBagAllocationService;

    @Autowired
    private BloodBagRepository bloodBagRepository;

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Test
    void shouldAllocateCompatibleBloodBag() {
        BloodRequest request = bloodRequestRepository.save(BloodRequest.builder()
                .hospitalName("Hospital Central")
                .bloodType(BloodType.O)
                .rhFactor(RhFactor.POSITIVE)
                .componentType(ComponentType.RED_BLOOD_CELLS)
                .quantity(1)
                .build());

        BloodBag bag = bloodBagRepository.save(BloodBag.builder()
                .bloodType(BloodType.O)
                .rhFactor(RhFactor.POSITIVE)
                .componentType(ComponentType.RED_BLOOD_CELLS)
                .collectionDate(LocalDate.now().minusDays(10))
                .expirationDate(LocalDate.now().plusDays(30))
                .volume(450)
                .status(BloodBagStatus.DISPONIVEL)
                .build());

        BloodBagAllocationResponse response = bloodBagAllocationService.allocateCompatibleBag(request.getId());

        assertTrue(response.isAllocated());
        assertEquals(bag.getId(), response.getBagId());
        assertEquals(BloodBagStatus.ALOCADA, bloodBagRepository.findById(bag.getId()).orElseThrow().getStatus());
    }

    @Test
    void shouldChooseEarliestExpirationDate() {
        BloodRequest request = bloodRequestRepository.save(BloodRequest.builder()
                .hospitalName("Hospital Central")
                .bloodType(BloodType.O)
                .rhFactor(RhFactor.POSITIVE)
                .componentType(ComponentType.RED_BLOOD_CELLS)
                .quantity(1)
                .build());

        BloodBag earlier = bloodBagRepository.save(BloodBag.builder()
                .bloodType(BloodType.O)
                .rhFactor(RhFactor.POSITIVE)
                .componentType(ComponentType.RED_BLOOD_CELLS)
                .collectionDate(LocalDate.now().minusDays(20))
                .expirationDate(LocalDate.now().plusDays(5))
                .volume(450)
                .status(BloodBagStatus.DISPONIVEL)
                .build());

        BloodBag later = bloodBagRepository.save(BloodBag.builder()
                .bloodType(BloodType.O)
                .rhFactor(RhFactor.POSITIVE)
                .componentType(ComponentType.RED_BLOOD_CELLS)
                .collectionDate(LocalDate.now().minusDays(20))
                .expirationDate(LocalDate.now().plusDays(10))
                .volume(450)
                .status(BloodBagStatus.DISPONIVEL)
                .build());

        BloodBagAllocationResponse response = bloodBagAllocationService.allocateCompatibleBag(request.getId());

        assertTrue(response.isAllocated());
        assertEquals(earlier.getId(), response.getBagId());
        assertEquals(BloodBagStatus.ALOCADA, bloodBagRepository.findById(earlier.getId()).orElseThrow().getStatus());
    }

    @Test
    void shouldIgnoreExpiredBag() {
        BloodRequest request = bloodRequestRepository.save(BloodRequest.builder()
                .hospitalName("Hospital Central")
                .bloodType(BloodType.O)
                .rhFactor(RhFactor.POSITIVE)
                .componentType(ComponentType.RED_BLOOD_CELLS)
                .quantity(1)
                .build());

        bloodBagRepository.save(BloodBag.builder()
                .bloodType(BloodType.O)
                .rhFactor(RhFactor.POSITIVE)
                .componentType(ComponentType.RED_BLOOD_CELLS)
                .collectionDate(LocalDate.now().minusDays(40))
                .expirationDate(LocalDate.now().minusDays(1))
                .volume(450)
                .status(BloodBagStatus.DISPONIVEL)
                .build());

        BloodBagAllocationResponse response = bloodBagAllocationService.allocateCompatibleBag(request.getId());

        assertFalse(response.isAllocated());
        assertEquals("Nenhuma bolsa compatível disponível no momento.", response.getMessage());
    }

    @Test
    void shouldIgnoreIncompatibleBag() {
        BloodRequest request = bloodRequestRepository.save(BloodRequest.builder()
                .hospitalName("Hospital Central")
                .bloodType(BloodType.A)
                .rhFactor(RhFactor.POSITIVE)
                .componentType(ComponentType.RED_BLOOD_CELLS)
                .quantity(1)
                .build());

        bloodBagRepository.save(BloodBag.builder()
                .bloodType(BloodType.B)
                .rhFactor(RhFactor.POSITIVE)
                .componentType(ComponentType.RED_BLOOD_CELLS)
                .collectionDate(LocalDate.now().minusDays(10))
                .expirationDate(LocalDate.now().plusDays(20))
                .volume(450)
                .status(BloodBagStatus.DISPONIVEL)
                .build());

        BloodBagAllocationResponse response = bloodBagAllocationService.allocateCompatibleBag(request.getId());

        assertFalse(response.isAllocated());
    }

    @Test
    void shouldReturnNoAllocationWhenNoCompatibleBagIsAvailable() {
        BloodRequest request = bloodRequestRepository.save(BloodRequest.builder()
                .hospitalName("Hospital Central")
                .bloodType(BloodType.AB)
                .rhFactor(RhFactor.NEGATIVE)
                .componentType(ComponentType.RED_BLOOD_CELLS)
                .quantity(1)
                .build());

        BloodBag bag = bloodBagRepository.save(BloodBag.builder()
                .bloodType(BloodType.O)
                .rhFactor(RhFactor.POSITIVE)
                .componentType(ComponentType.RED_BLOOD_CELLS)
                .collectionDate(LocalDate.now().minusDays(5))
                .expirationDate(LocalDate.now().plusDays(5))
                .volume(450)
                .status(BloodBagStatus.DISPONIVEL)
                .build());

        BloodBagAllocationResponse response = bloodBagAllocationService.allocateCompatibleBag(request.getId());

        assertFalse(response.isAllocated());
        assertEquals(BloodBagStatus.DISPONIVEL, bloodBagRepository.findById(bag.getId()).orElseThrow().getStatus());
    }

    @Test
    void shouldIgnoreAlreadyAllocatedBag() {
        BloodRequest request = bloodRequestRepository.save(BloodRequest.builder()
                .hospitalName("Hospital Central")
                .bloodType(BloodType.A)
                .rhFactor(RhFactor.NEGATIVE)
                .componentType(ComponentType.RED_BLOOD_CELLS)
                .quantity(1)
                .build());

        BloodBag bag = bloodBagRepository.save(BloodBag.builder()
                .bloodType(BloodType.A)
                .rhFactor(RhFactor.NEGATIVE)
                .componentType(ComponentType.RED_BLOOD_CELLS)
                .collectionDate(LocalDate.now().minusDays(5))
                .expirationDate(LocalDate.now().plusDays(20))
                .volume(450)
                .status(BloodBagStatus.ALOCADA)
                .build());

        BloodBagAllocationResponse response = bloodBagAllocationService.allocateCompatibleBag(request.getId());

        assertFalse(response.isAllocated());
        assertEquals(BloodBagStatus.ALOCADA, bloodBagRepository.findById(bag.getId()).orElseThrow().getStatus());
    }
}
