package io.manager.policy.domain.repository;

import io.manager.policy.domain.model.Assistances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssistancesRepository extends JpaRepository<Assistances, String> {}