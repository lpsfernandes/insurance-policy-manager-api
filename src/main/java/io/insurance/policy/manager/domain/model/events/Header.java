package io.insurance.policy.manager.domain.model.events;

import lombok.Builder;

@Builder
public record Header(String traceId) {
}
