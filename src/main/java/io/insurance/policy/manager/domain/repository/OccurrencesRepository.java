package io.insurance.policy.manager.domain.repository;

import io.insurance.policy.manager.domain.model.Occurrences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OccurrencesRepository extends JpaRepository<Occurrences, String> {
}
