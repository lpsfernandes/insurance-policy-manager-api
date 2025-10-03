package io.manager.policy.domain.repository;

import io.manager.policy.domain.model.Coverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoverageRepository extends JpaRepository<Coverage, String> {}