package io.insurance.policy.manager.domain.model;

import io.insurance.policy.manager.application.service.ProcessingStatus;
import io.insurance.policy.manager.domain.model.enums.RiskClassification;
import io.insurance.policy.manager.domain.model.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_policies")
public class Policy {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private String id;

    @Column(name = "client_id", nullable = false)
    String clientId;

    @Column(name = "product_id", nullable = false)
    Long productId;

    @Column(name = "category", length = 50, nullable = false)
    String category;

    @Column(name = "sales_channel", length = 50)
    String salesChannel;

    @Column(name = "payment_method", length = 50, nullable = false)
    String paymentMethod;

    @Column(name = "insured_amount", nullable = false)
    Long insuredAmount;

    @Column(name = "monthly_premium", nullable = false)
    Long monthlyPremium;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_classification")
    RiskClassification riskClassification;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    ZonedDateTime createdAt;

    @Column(name = "finished_at")
    ZonedDateTime finishedAt;

    @Column(name = "payment_date")
    ZonedDateTime paymentDate;

    @Column(name = "subscription_date")
    ZonedDateTime subscriptionDate;

    @Column
    String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    ProcessingStatus processingStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status;

    @OneToMany(mappedBy = "policyId", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    Set<StatusHistory> history;

    @OneToMany(mappedBy = "policyId", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Coverage> coverages;

    @OneToMany(mappedBy = "policyId", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Assistances> assistances;

}
