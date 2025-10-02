package io.manager.policy.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "tb_coverage")
public class Coverage {

    @Column(name = "policy_id", nullable = false)
    String policyId;

    @Column(name = "type_coverage", nullable = false)
    String typeCoverage;

    @Column(name = "insured_amount", nullable = false)
    BigDecimal insuredAmount;

}
