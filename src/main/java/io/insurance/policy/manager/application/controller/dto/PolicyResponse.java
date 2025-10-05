package io.insurance.policy.manager.application.controller.dto;

import io.insurance.policy.manager.application.controller.dto.enums.Category;
import io.insurance.policy.manager.application.controller.dto.enums.PaymentMethod;
import io.insurance.policy.manager.application.controller.dto.enums.SalesChannel;
import io.insurance.policy.manager.domain.model.Assistances;
import io.insurance.policy.manager.domain.model.Coverage;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.RiskClassification;
import io.insurance.policy.manager.domain.model.enums.Status;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static io.insurance.policy.manager.application.util.Helper.DEFAULT_DECIMAL_PLACES;

public record PolicyResponse(
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
        ZonedDateTime paymentDate,
        ZonedDateTime subscriptionDate,
        String reason,
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
                policy.getPaymentDate(),
                policy.getSubscriptionDate(),
                policy.getReason(),
                policy.getCreatedAt(),
                policy.getFinishedAt(),
                policy.getHistory()
                        .stream()
                        .map(history ->
                                new StateHistory(history.getStatus().name(), history.getCreatedAt()))
                        .collect(Collectors.toCollection(() ->
                                        new TreeSet<>(Comparator.comparing(StateHistory::timestamp).reversed()))
                        ), SalesChannel.valueOf(policy.getSalesChannel()));
    }

}