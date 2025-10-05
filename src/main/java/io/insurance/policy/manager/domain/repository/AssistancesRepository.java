package io.insurance.policy.manager.domain.repository;

import io.insurance.policy.manager.domain.model.Assistances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssistancesRepository extends JpaRepository<Assistances, String> {}