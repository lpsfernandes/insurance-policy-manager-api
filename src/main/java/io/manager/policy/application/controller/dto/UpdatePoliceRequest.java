package io.manager.policy.application.controller.dto;

import io.manager.policy.domain.model.enums.Status;
import jakarta.validation.constraints.NotNull;

public record UpdatePoliceRequest(@NotNull Status status) {
}
