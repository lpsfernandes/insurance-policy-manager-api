package io.insurance.policy.manager.domain.model;

import io.insurance.policy.manager.domain.model.enums.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusTest {

    @Test
    void testReceivedTransitions() {
        assertTrue(Status.RECEIVED.canTransitionTo(Status.VALIDATED));
        assertTrue(Status.RECEIVED.canTransitionTo(Status.CANCELED));
        assertFalse(Status.RECEIVED.canTransitionTo(Status.REJECTED));
        assertFalse(Status.RECEIVED.canTransitionTo(Status.APPROVED));
        assertFalse(Status.RECEIVED.canTransitionTo(Status.PENDING));
    }

    @Test
    void testValidatedTransitions() {
        assertTrue(Status.VALIDATED.canTransitionTo(Status.PENDING));
        assertTrue(Status.VALIDATED.canTransitionTo(Status.REJECTED));
        assertFalse(Status.VALIDATED.canTransitionTo(Status.APPROVED));
        assertFalse(Status.VALIDATED.canTransitionTo(Status.CANCELED));
        assertFalse(Status.VALIDATED.canTransitionTo(Status.RECEIVED));
    }

    @Test
    void testPendingTransitions() {
        assertTrue(Status.PENDING.canTransitionTo(Status.APPROVED));
        assertTrue(Status.PENDING.canTransitionTo(Status.REJECTED));
        assertTrue(Status.PENDING.canTransitionTo(Status.CANCELED));
        assertTrue(Status.PENDING.canTransitionTo(Status.PENDING)); // auto-transição
        assertFalse(Status.PENDING.canTransitionTo(Status.VALIDATED));
        assertFalse(Status.PENDING.canTransitionTo(Status.RECEIVED));
    }

    @Test
    void testRejectedTransitions() {
        for (Status target : Status.values()) {
            assertFalse(Status.REJECTED.canTransitionTo(target));
        }
    }

    @Test
    void testApprovedTransitions() {
        for (Status target : Status.values()) {
            assertFalse(Status.APPROVED.canTransitionTo(target));
        }
    }

    @Test
    void testCanceledTransitions() {
        for (Status target : Status.values()) {
            assertFalse(Status.CANCELED.canTransitionTo(target));
        }
    }
}