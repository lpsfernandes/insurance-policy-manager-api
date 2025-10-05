package io.insurance.policy.manager.boundaries.driving.consumer.dto;

import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public record InsuranceSubscriptionEvent(@NotNull String id,
                                         @NotNull String orderId,
                                         ZonedDateTime subscriptionDateTime,
                                         String reason,
                                         @NotNull Status status) {
    public enum Status { APPROVED, REJECTED }

    public boolean insuranceSubscriptionCompleted(){
        return this.status == Status.APPROVED;
    }
}
