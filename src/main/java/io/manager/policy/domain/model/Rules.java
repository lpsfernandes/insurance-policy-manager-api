package io.manager.policy.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_rules")
public class Rules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_classification", nullable = false)
    RiskClassification riskClassification;

    @Column(name = "insured_amount_limit", nullable = false)
    Long insuredAmountLimit;

}
