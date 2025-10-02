package io.manager.policy.api.dto;

import io.manager.policy.api.dto.enums.Category;
import io.manager.policy.api.dto.enums.PaymentMethod;
import io.manager.policy.api.dto.enums.SalesChannel;
import io.manager.policy.domain.model.Assistances;
import io.manager.policy.domain.model.Policy;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record PolicyResponse(
        String id,
        String clientId,
        Long productId,
        Category category,
        BigDecimal monthlyPremium,
        BigDecimal insuredAmount,
        Set<Map<String, BigDecimal>> coverages,
        Set<String> assistances,
        PaymentMethod paymentMethod,
        ZonedDateTime createdAt,
        ZonedDateTime finishedAt,
        Set<StateHistory> history,
        SalesChannel salesChannel
) {
    public record StateHistory(
            String status,
            ZonedDateTime timestamp
    ) {}

    public PolicyResponse(Policy policy){
        this(policy.getId(),
                policy.getClientId(),
                policy.getProductId(),
                Category.valueOf(policy.getCategory()),
                policy.getMonthlyPremium(),
                policy.getInsuredAmount(),
                policy.getCoverages()
                        .stream()
                        .map(coverage -> {
                                    Map<String, BigDecimal> retCoverage = new HashMap<>();
                                    retCoverage.put(coverage.getTypeCoverage(), coverage.getInsuredAmount());
                                    return retCoverage;
                                }
                        )
                        .collect(Collectors.toSet()),
                policy.getAssistances()
                        .stream()
                        .map(Assistances::getAssistance)
                        .collect(Collectors.toSet()),
                PaymentMethod.valueOf(policy.getPaymentMethod()),
                policy.getCreatedAt(),
                policy.getFinishedAt(),
                policy.getHistory()
                        .stream()
                        .map(history ->
                                new StateHistory(history.getStatus().name(), history.getTimestamp()))
                        .collect(Collectors.toSet()),
                SalesChannel.valueOf(policy.getSalesChannel()));
    }

}