package io.manager.policy.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "tb_coverage")
public class Coverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_id", nullable = false)
    String policyId;

    @Column(name = "type_coverage", nullable = false)
    String typeCoverage;

    @Column(name = "insured_amount", nullable = false)
    Long insuredAmount;

}
