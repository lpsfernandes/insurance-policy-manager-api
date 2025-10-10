package io.insurance.policy.manager.domain.repository;

import io.insurance.policy.manager.application.service.ProcessingStatus;
import io.insurance.policy.manager.domain.model.Policy;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, String> {
    Optional<Policy> findById(@NonNull String id);
    Page<Policy> findByClientId(@NonNull String clientId, PageRequest pageable);
    List<Policy> findByProcessingStatus(ProcessingStatus processingStatus, Pageable pageable);

}
