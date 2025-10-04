package io.manager.policy.domain.model.events;

import io.manager.policy.application.controller.dto.enums.Category;
import io.manager.policy.application.controller.dto.enums.PaymentMethod;
import io.manager.policy.application.controller.dto.enums.SalesChannel;
import io.manager.policy.domain.model.*;
import io.manager.policy.domain.model.enums.RiskClassification;
import io.manager.policy.domain.model.enums.Status;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static io.manager.policy.application.util.Helper.DEFAULT_DECIMAL_PLACES;


public record EventBody(
        String id,
        String clientId,
        Long productId,
        Category category,
        Status status,
        RiskClassification riskClassification,
        BigDecimal monthlyPremium,
        BigDecimal insuredAmount,
        PaymentMethod paymentMethod,
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
                Category.valueOf(policy.getCategory()),
                policy.getStatus(),
                policy.getRiskClassification(),
                new BigDecimal(policy.getMonthlyPremium()).movePointLeft(DEFAULT_DECIMAL_PLACES),
                new BigDecimal(policy.getInsuredAmount()).movePointLeft(DEFAULT_DECIMAL_PLACES),
                PaymentMethod.valueOf(policy.getPaymentMethod()),
                policy.getCreatedAt(),
                policy.getFinishedAt(),
                SalesChannel.valueOf(policy.getSalesChannel()));
    }

}