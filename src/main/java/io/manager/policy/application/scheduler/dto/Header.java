package io.manager.policy.application.scheduler.dto;

import lombok.Builder;

@Builder
public record Header(String traceId) {
}
