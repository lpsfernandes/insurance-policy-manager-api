package io.manager.policy.application.scheduler.dto;

import io.manager.policy.application.controller.dto.enums.Category;
import io.manager.policy.application.controller.dto.enums.PaymentMethod;
import io.manager.policy.application.controller.dto.enums.SalesChannel;
import io.manager.policy.domain.model.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.manager.policy.application.util.Helper.DEFAULT_DECIMAL_PLACES;


public record Event(
        String id,
        String clientId,
        Long productId,
        Category category,
        Status status,
        RiskClassification riskClassification,
        BigDecimal monthlyPremium,
        BigDecimal insuredAmount,
        Map<String, BigDecimal> coverages,
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

    public Event(Policy policy){
        this(policy.getId(),
                policy.getClientId(),
                policy.getProductId(),
                Category.valueOf(policy.getCategory()),
                policy.getStatus(),
                policy.getRiskClassification(),
                new BigDecimal(policy.getMonthlyPremium()).movePointLeft(DEFAULT_DECIMAL_PLACES),
                new BigDecimal(policy.getInsuredAmount()).movePointLeft(DEFAULT_DECIMAL_PLACES),
                policy.getCoverages()
                        .stream()
                        .collect(Collectors.toMap(Coverage::getTypeCoverage,
                                c -> new BigDecimal(c.getInsuredAmount()).movePointLeft(DEFAULT_DECIMAL_PLACES))),
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
                                new StateHistory(history.getStatus().name(), history.getCreatedAt()))
                        .collect(Collectors.toSet()),
                SalesChannel.valueOf(policy.getSalesChannel()));
    }

}