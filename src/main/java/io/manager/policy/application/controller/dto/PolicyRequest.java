package io.manager.policy.application.controller.dto;

import io.manager.policy.application.controller.dto.enums.Category;
import io.manager.policy.application.controller.dto.enums.PaymentMethod;
import io.manager.policy.application.controller.dto.enums.SalesChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public record PolicyRequest(
        @NotNull @NotBlank String clientId,
        @NotNull Long productId,
        @NotNull Category category,
        @NotNull SalesChannel salesChannel,
        @NotNull PaymentMethod paymentMethod,
        @NotNull BigDecimal insuredAmount,
        @NotNull BigDecimal monthlyPremium,
        @NotNull Map<String, BigDecimal> coverages,
        Set<String> assistances
) {}
