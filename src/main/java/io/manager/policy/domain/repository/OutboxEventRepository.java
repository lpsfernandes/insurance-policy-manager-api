package io.manager.policy.domain.repository;

import io.manager.policy.domain.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    int deleteByCreatedAtBefore(ZonedDateTime limit);
}