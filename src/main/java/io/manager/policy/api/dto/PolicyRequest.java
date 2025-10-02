package io.manager.policy.api.dto;

import io.manager.policy.api.dto.enums.Category;
import io.manager.policy.api.dto.enums.PaymentMethod;
import io.manager.policy.api.dto.enums.SalesChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record PolicyRequest(
        @NotNull @NotBlank String clientId,
        @NotNull Long productId,
        @NotNull Category category,
        @NotNull SalesChannel salesChannel,
        @NotNull PaymentMethod paymentMethod,
        @NotNull BigDecimal insuredAmount,
        @NotNull BigDecimal monthlyPremium,
        @NotNull List<Coverage> coverages,
        List<String> assistances
) {
    public record Coverage(
            String typeCoverage,
            BigDecimal insuredAmount
    ) {}
}
