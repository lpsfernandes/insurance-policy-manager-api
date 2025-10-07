package io.insurance.policy.manager.boundaries.driven.http.dto;

import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;
import java.util.List;

public record RiskAnalysisResponse(
        @NotNull String orderId,
        String clientId,
        ZonedDateTime analyzedAt,
        @NotNull String classification,
        List<Occurrence> occurrences
) {
    public record Occurrence(
            String id,
            long productId,
            String type,
            String description,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {}
}
