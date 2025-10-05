package io.insurance.policy.manager.domain.model.enums;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum Status {
    RECEIVED,
    VALIDATED,
    PENDING,
    REJECTED,
    APPROVED,
    CANCELED;

    private Set<Status> allowedTransitions;

    static {
        RECEIVED.allowedTransitions = EnumSet.of(VALIDATED, CANCELED);
        VALIDATED.allowedTransitions = EnumSet.of(PENDING, REJECTED);
        PENDING.allowedTransitions = EnumSet.of(APPROVED, REJECTED, CANCELED, PENDING);
        REJECTED.allowedTransitions = EnumSet.noneOf(Status.class);
        APPROVED.allowedTransitions = EnumSet.noneOf(Status.class);
        CANCELED.allowedTransitions = EnumSet.noneOf(Status.class);
    }

    public boolean canTransitionTo(Status target) {
        return allowedTransitions.contains(target);
    }

}

