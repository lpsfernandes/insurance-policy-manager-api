package io.manager.policy.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_assistances")
public class Assistances {

    @Column(name = "policy_id", nullable = false)
    String policyId;

    @Column(nullable = false)
    String assistance;

}
