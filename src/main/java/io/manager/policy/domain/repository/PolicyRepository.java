package io.manager.policy.domain.repository;

import io.manager.policy.domain.model.Policy;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyRepository extends CrudRepository<Policy, String> {

    Optional<Policy> findById(@NonNull String id);
}
