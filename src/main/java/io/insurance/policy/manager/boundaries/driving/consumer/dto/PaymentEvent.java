package io.insurance.policy.manager.boundaries.driving.consumer.dto;

import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public record PaymentEvent(@NotNull String id,
                           @NotNull String orderId,
                           ZonedDateTime paymentDateTime,
                           String reason,
                           @NotNull Status status) {
    public enum Status { APPROVED, REJECTED }

    public boolean paymentCompleted(){
        return this.status == Status.APPROVED;
    }
}
