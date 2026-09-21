package com.jackops.rotavital.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jackops.rotavital.dto.BloodBagAllocationResponse;
import com.jackops.rotavital.model.BloodBag;
import com.jackops.rotavital.model.BloodBagStatus;
import com.jackops.rotavital.model.BloodRequest;
import com.jackops.rotavital.model.BloodType;
import com.jackops.rotavital.model.RhFactor;
import com.jackops.rotavital.repository.BloodBagRepository;
import com.jackops.rotavital.repository.BloodRequestRepository;

@Service
public class BloodBagAllocationService {

    private final BloodBagRepository bloodBagRepository;
    private final BloodRequestRepository bloodRequestRepository;

    public BloodBagAllocationService(BloodBagRepository bloodBagRepository, BloodRequestRepository bloodRequestRepository) {
        this.bloodBagRepository = bloodBagRepository;
        this.bloodRequestRepository = bloodRequestRepository;
    }

    @Transactional
    public BloodBagAllocationResponse allocateCompatibleBag(Long requestId) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada."));

        List<BloodBag> candidateBags = bloodBagRepository.findAll().stream()
                .filter(bag -> bag.getStatus() == BloodBagStatus.DISPONIVEL)
                .filter(bag -> !bag.getExpirationDate().isBefore(LocalDate.now()))
                .filter(bag -> isCompatible(request, bag))
                .filter(bag -> bag.getComponentType() == request.getComponentType())
                .sorted(Comparator.comparing(BloodBag::getExpirationDate))
                .toList();

        if (candidateBags.isEmpty()) {
            return new BloodBagAllocationResponse(null, false, "Nenhuma bolsa compatível disponível no momento.");
        }

        BloodBag selectedBag = candidateBags.getFirst();
        selectedBag.setStatus(BloodBagStatus.ALOCADA);
        bloodBagRepository.save(selectedBag);

        return new BloodBagAllocationResponse(selectedBag.getId(), true,
                "Bolsa alocada com sucesso conforme compatibilidade ABO/Rh e regra FEFO.");
    }

    public boolean isCompatible(BloodRequest request, BloodBag bag) {
        return isCompatibleByBloodType(request.getBloodType(), request.getRhFactor(), bag.getBloodType(), bag.getRhFactor());
    }

    public boolean isCompatibleByBloodType(BloodType requestedType, RhFactor requestedRh, BloodType bagType, RhFactor bagRh) {
        if (requestedRh == RhFactor.NEGATIVE && bagRh == RhFactor.POSITIVE) {
            return false;
        }

        if (!canReceiveBloodType(requestedType, bagType)) {
            return false;
        }

        if (requestedRh == RhFactor.POSITIVE) {
            return true;
        }

        return bagRh == RhFactor.NEGATIVE;
    }

    private boolean canReceiveBloodType(BloodType recipientType, BloodType donorType) {
        if (recipientType == BloodType.O) {
            return donorType == BloodType.O;
        }
        if (recipientType == BloodType.A) {
            return donorType == BloodType.O || donorType == BloodType.A;
        }
        if (recipientType == BloodType.B) {
            return donorType == BloodType.O || donorType == BloodType.B;
        }
        return donorType == BloodType.O || donorType == BloodType.A || donorType == BloodType.B || donorType == BloodType.AB;
    }
}
