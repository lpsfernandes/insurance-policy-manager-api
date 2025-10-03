package io.manager.policy.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_risk_analysis")
public class RiskAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "analyzed_at", nullable = false)
    private ZonedDateTime analyzedAt;

    @Column(nullable = false)
    private String classification;

    @Column(name = "policy_id", nullable = false)
    String policyId;

    @OneToMany(mappedBy = "riskAnalysisId", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Occurrences> occurrences;

}
