package io.manager.policy.domain.model.events;

import lombok.Builder;

@Builder
public record Header(String traceId) {
}
