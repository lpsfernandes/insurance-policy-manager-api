package io.insurance.policy.manager.domain.model.events;

import io.insurance.policy.manager.boundaries.driving.http.dto.enums.Category;
import io.insurance.policy.manager.boundaries.driving.http.dto.enums.PaymentMethod;
import io.insurance.policy.manager.boundaries.driving.http.dto.enums.SalesChannel;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.RiskClassification;
import io.insurance.policy.manager.domain.model.enums.Status;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static io.insurance.policy.manager.application.util.Helper.DEFAULT_DECIMAL_PLACES;


public record EventBody(
        String orderId,
        String clientId,
        Long productId,
        Category category,
        Status status,
        RiskClassification riskClassification,
        BigDecimal monthlyPremium,
        BigDecimal insuredAmount,
        PaymentMethod paymentMethod,
        ZonedDateTime paymentDate,
        ZonedDateTime subscriptionDate,
        String reason,
        ZonedDateTime createdAt,
        ZonedDateTime finishedAt,
        SalesChannel salesChannel
) {
    public record StateHistory(
            String status,
            ZonedDateTime timestamp
    ) {}

    public EventBody(Policy policy){
        this(policy.getId(),
                policy.getClientId(),
                policy.getProductId(),
                policy.getCategory() != null ? Category.valueOf(policy.getCategory()) : null,
                policy.getStatus(),
                policy.getRiskClassification(),
                new BigDecimal(policy.getMonthlyPremium()).movePointLeft(DEFAULT_DECIMAL_PLACES),
                new BigDecimal(policy.getInsuredAmount()).movePointLeft(DEFAULT_DECIMAL_PLACES),
                policy.getPaymentMethod() != null ? PaymentMethod.valueOf(policy.getPaymentMethod()) : null,
                policy.getPaymentDate(),
                policy.getSubscriptionDate(),
                policy.getReason(),
                policy.getCreatedAt(),
                policy.getFinishedAt(),
                policy.getSalesChannel() != null ? SalesChannel.valueOf(policy.getSalesChannel()) : null);
    }

}