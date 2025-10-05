package io.insurance.policy.manager.domain.repository;

import io.insurance.policy.manager.domain.model.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    @Query("SELECT e FROM OutboxEvent e " +
            "WHERE e.maxProcessingTime <= :now OR maxProcessingTime IS NULL")
    List<OutboxEvent> findByEventsPendingProcessing(
            ZonedDateTime now,
            Pageable pageable
    );

}