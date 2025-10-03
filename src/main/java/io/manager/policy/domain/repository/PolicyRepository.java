package io.manager.policy.domain.repository;

import io.manager.policy.domain.model.Policy;
import io.manager.policy.domain.model.Status;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, String> {
    Optional<Policy> findById(@NonNull String id);
    Page<Policy> findByClientId(@NonNull String clientId, PageRequest pageable);

    @Query("SELECT p FROM Policy p " +
            "WHERE p.status = :status " +
            "AND (p.maxProcessingTime <= :now OR maxProcessingTime IS NULL)")
    List<Policy> findByPoliciesPendingProcessing(
            Status status,
            ZonedDateTime now,
            Pageable pageable
    );

}
