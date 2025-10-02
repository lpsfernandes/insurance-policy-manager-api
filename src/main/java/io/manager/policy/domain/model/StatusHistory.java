package io.manager.policy.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "tb_status_history")
public class StatusHistory {

    @Column(name = "policy_id", nullable = false)
    String policyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    ZonedDateTime timestamp;

}
