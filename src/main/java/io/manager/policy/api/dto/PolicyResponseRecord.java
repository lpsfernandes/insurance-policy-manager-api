package io.manager.policy.api.dto;

import io.manager.policy.api.dto.enums.Category;
import io.manager.policy.api.dto.enums.PaymentMethod;
import io.manager.policy.api.dto.enums.SalesChannel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record PolicyResponseRecord(
        String id,
        String clientId,
        Long productId,
        Category category,
        BigDecimal monthlyPremium,
        BigDecimal insuredAmount,
        List<Map<String, BigDecimal>> coverages,
        List<String> assistances,
        PaymentMethod paymentMethod,
        Instant createdAt,
        Instant finishedAt,
        List<StateHistory> history,
        SalesChannel salesChannel
) {
    public record StateHistory(
            Instant when,
            String fromState,
            String toState,
            String reason
    ) {}

}