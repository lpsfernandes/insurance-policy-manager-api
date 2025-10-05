package io.insurance.policy.manager.domain.repository;

import io.insurance.policy.manager.domain.model.RiskAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskAnalysisRepository extends JpaRepository<RiskAnalysis, String> {
}
