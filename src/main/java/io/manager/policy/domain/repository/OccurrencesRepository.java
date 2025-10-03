package io.manager.policy.domain.repository;

import io.manager.policy.domain.model.Occurrences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OccurrencesRepository extends JpaRepository<Occurrences, String> {
}
