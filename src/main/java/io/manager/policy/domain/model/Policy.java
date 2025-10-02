package io.manager.policy.domain.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Builder
@Entity
@Table(name = "tb_policies")
public class Policy {

    @Id
    private String id;

    @Column(name = "client_id", nullable = false)
    String clientId;

    @Column(name = "product_id", unique = true, nullable = false)
    Long productId;

    @Column(name = "category", length = 50, nullable = false)
    String category;

    @Column(name = "sales_channel", length = 50)
    String salesChannel;

    @Column(name = "payment_method", length = 50, nullable = false)
    String paymentMethod;

    @Column(name = "insured_amount", nullable = false)
    BigDecimal insuredAmount;

    @Column(name = "monthly_premium", nullable = false)
    BigDecimal monthlyPremium;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    ZonedDateTime createdAt;

    @Column(name = "finished_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    ZonedDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status;

    @OneToMany
    @JoinColumn(name = "policy_id", referencedColumnName = "policy_id", insertable = false, updatable = false)
    Set<StatusHistory> history;

    @OneToMany
    @JoinColumn(name = "policy_id", referencedColumnName = "policy_id", insertable = false, updatable = false)
    Set<Coverage> coverages;

    @OneToMany
    @JoinColumn(name = "policy_id", referencedColumnName = "policy_id", insertable = false, updatable = false)
    Set<Assistances> assistances;

}
