package io.manager.policy.domain.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Builder
@Data
@Entity
@Table(name = "tb_occurrences")
public class Occurrences  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "risk_analysis_id", nullable = false)
    private Long riskAnalysisId;

    @Column(name = "occurrence_id", nullable = false)
    private String occurrenceId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String description;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

}