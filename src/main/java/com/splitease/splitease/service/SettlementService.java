package com.splitease.splitease.service;

import com.splitease.splitease.dto.CreateSettlementRequest;
import com.splitease.splitease.dto.SettlementResponse;
import com.splitease.splitease.exception.ApiException;
import com.splitease.splitease.model.*;
import com.splitease.splitease.repository.ExpenseGroupRepository;
import com.splitease.splitease.repository.SettlementRepository;
import com.splitease.splitease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final ExpenseGroupRepository groupRepository;

    public SettlementResponse createSettlement(Long payerId,
                                               CreateSettlementRequest request) {

        User payer = userRepository.findById(payerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payer not found"));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Receiver not found"));

        ExpenseGroup group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Group not found"));

        Settlement settlement = Settlement.builder()
                .payer(payer)
                .receiver(receiver)
                .group(group)
                .amount(request.getAmount())
                .status(SettlementStatus.PENDING)
                .build();

        settlementRepository.save(settlement);

        return mapToResponse(settlement);
    }

    public List<SettlementResponse> getMySettlements(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        return settlementRepository.findByPayerOrReceiver(user, user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public SettlementResponse markAsPaid(Long settlementId) {

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Settlement not found"));

        if (settlement.getStatus() == SettlementStatus.PAID) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Settlement already paid");
        }

        settlement.setStatus(SettlementStatus.PAID);
        settlement.setSettledAt(LocalDateTime.now());

        settlementRepository.save(settlement);

        return mapToResponse(settlement);
    }

    private SettlementResponse mapToResponse(Settlement settlement) {

        return SettlementResponse.builder()
                .id(settlement.getId())
                .payerName(settlement.getPayer().getName())
                .receiverName(settlement.getReceiver().getName())
                .groupName(settlement.getGroup().getName())
                .amount(settlement.getAmount())
                .status(settlement.getStatus())
                .settledAt(settlement.getSettledAt())
                .build();
    }

}