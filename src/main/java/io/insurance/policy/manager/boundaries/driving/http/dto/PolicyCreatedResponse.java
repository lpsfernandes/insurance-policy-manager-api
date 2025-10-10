package io.insurance.policy.manager.boundaries.driving.http.dto;

import io.insurance.policy.manager.domain.model.Policy;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
public class PolicyCreatedResponse {
    private final String id;
    private final ZonedDateTime createdAt;

    public PolicyCreatedResponse(Policy policy){
        this.id = policy.getId();
        this.createdAt = policy.getCreatedAt();
    }
}
