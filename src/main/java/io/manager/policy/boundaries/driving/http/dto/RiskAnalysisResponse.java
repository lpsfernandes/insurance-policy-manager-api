package io.manager.policy.boundaries.driving.http.dto;

import java.time.ZonedDateTime;
import java.util.List;

public record RiskAnalysisResponse(
        String orderId,
        String clientId,
        ZonedDateTime analyzedAt,
        String classification,
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
