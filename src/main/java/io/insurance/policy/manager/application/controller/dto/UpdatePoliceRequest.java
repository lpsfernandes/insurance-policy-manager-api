package io.insurance.policy.manager.application.controller.dto;

import io.insurance.policy.manager.domain.model.enums.Status;
import jakarta.validation.constraints.NotNull;

public record UpdatePoliceRequest(@NotNull Status status) {
}
