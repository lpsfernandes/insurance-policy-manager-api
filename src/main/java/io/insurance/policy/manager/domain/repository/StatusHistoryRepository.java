package io.insurance.policy.manager.domain.repository;

import io.insurance.policy.manager.domain.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, String> {}