package io.insurance.policy.manager.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.insurance.policy.manager.application.controller.dto.enums.PaymentMethod;
import io.insurance.policy.manager.application.controller.dto.enums.SalesChannel;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.StatusHistory;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.OutboxEventRepository;
import io.insurance.policy.manager.domain.repository.PolicyRepository;
import io.insurance.policy.manager.domain.repository.StatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandleStatusTests {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    @Mock
    private ObjectMapper objectMapper;

    private HandleStatusTestImpl handleStatus;

    private Policy policy;

    @BeforeEach
    void setUp() {
        handleStatus = new HandleStatusTestImpl(outboxEventRepository, statusHistoryRepository, objectMapper);

        policy = Policy.builder()
                .id("policy-789")
                .clientId("client-123")
                .productId(1L)
                .category("AUTO")
                .paymentMethod(PaymentMethod.PIX.name())
                .salesChannel(SalesChannel.WEB_SITE.name())
                .insuredAmount(100000L)
                .monthlyPremium(10000L)
                .status(Status.RECEIVED)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();
    }

    @Test
    void testInsertOutboxEventWithPolicyCreatedAt() throws Exception {
        String json = "{\"id\":\"policy-123\"}";
        when(objectMapper.writeValueAsString(any())).thenReturn(json);

        handleStatus.insertOutboxEvent(policy);

        verify(outboxEventRepository).save(argThat(event ->
                event.getEventJson().equals(json) &&
                        event.getCreatedAt().equals(policy.getCreatedAt())
        ));
    }

    @Test
    void testInsertOutboxEventWithCustomDate() throws Exception {
        String json = "{\"id\":\"policy-123\"}";
        ZonedDateTime customDate = ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1);
        when(objectMapper.writeValueAsString(any())).thenReturn(json);

        handleStatus.insertOutboxEvent(policy, customDate);

        verify(outboxEventRepository).save(argThat(event ->
                event.getEventJson().equals(json) &&
                        event.getCreatedAt().equals(customDate)
        ));
    }

    @Test
    void testInsertOutboxEventShouldHandleJsonException() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("erro") {});

        handleStatus.insertOutboxEvent(policy);

        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldInsertStatusHistoryCorrectly() {

        String policyId = "policy-123";
        Status status = Status.PENDING;

        ArgumentCaptor<StatusHistory> captor = ArgumentCaptor.forClass(StatusHistory.class);

        handleStatus.insertStatusHistory(policyId, status);

        verify(statusHistoryRepository).save(captor.capture());
        StatusHistory saved = captor.getValue();

        assertEquals(policyId, saved.getPolicyId());
        assertEquals(status, saved.getStatus());
        assertNotNull(saved.getCreatedAt());
        assertEquals(ZoneId.of("UTC"), saved.getCreatedAt().getZone());
    }


    public static class HandleStatusTestImpl extends HandleStatus {

        public HandleStatusTestImpl(OutboxEventRepository outboxEventRepository, StatusHistoryRepository statusHistoryRepository, ObjectMapper objectMapper) {
            this.outboxEventRepository = outboxEventRepository;
            this.objectMapper = objectMapper;
            this.statusHistoryRepository = statusHistoryRepository;
        }
    }

}

