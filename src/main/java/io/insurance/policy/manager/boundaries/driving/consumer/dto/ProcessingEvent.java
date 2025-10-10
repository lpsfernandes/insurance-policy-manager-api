package io.insurance.policy.manager.boundaries.driving.consumer.dto;

import jakarta.validation.constraints.NotNull;

public record ProcessingEvent(@NotNull String policyId) {
}
