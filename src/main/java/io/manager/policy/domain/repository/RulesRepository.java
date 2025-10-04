package io.manager.policy.domain.repository;

import io.manager.policy.domain.model.enums.RiskClassification;
import io.manager.policy.domain.model.Rules;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RulesRepository extends JpaRepository<Rules, String> {
    List<Rules> findByRiskClassification(@NonNull RiskClassification riskClassification);
}
