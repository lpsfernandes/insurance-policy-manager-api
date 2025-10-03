package io.manager.policy.domain.repository;

import io.manager.policy.domain.model.RiskAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskAnalysisRepository extends JpaRepository<RiskAnalysis, String> {
}
