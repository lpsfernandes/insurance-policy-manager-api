package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.application.service.interfaces.IRiskAnalysisService;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.RiskClassification;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PolicyValidationServiceTests {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private IRiskAnalysisService riskAnalysisService;

    private PolicyValidationService serviceSpy;

    @BeforeEach
    void setUp() {
        var service = new PolicyValidationService(Duration.ofMinutes(1), policyRepository, riskAnalysisService);
        serviceSpy = Mockito.spy(service);
    }

    @Test
    void testClassifyRiskProcessesPolicies() throws InterruptedException {
        doNothing().when(serviceSpy).insertOutboxEvent(any(), any());
        doNothing().when(serviceSpy).insertStatusHistory(any(), any());

        Policy policy = new Policy();
        policy.setId("policy-001");
        policy.setStatus(Status.RECEIVED);

        when(policyRepository.findByPoliciesPendingProcessing(eq(Status.RECEIVED), any(), any()))
                .thenReturn(List.of(policy));

        when(riskAnalysisService.analyzeRisk(policy)).thenReturn(RiskClassification.HIGH_RISK);

        serviceSpy.classifyRisk();

        // Aguarda execução assíncrona
        Thread.sleep(500);

        verify(policyRepository, atLeastOnce()).save(policy);
        assertEquals(Status.VALIDATED, policy.getStatus());
        assertEquals(RiskClassification.HIGH_RISK, policy.getRiskClassification());
        verify(serviceSpy).insertOutboxEvent(any(), any());
        verify(serviceSpy).insertStatusHistory(any(), any());
    }

    @Test
    void testInitSetsMaxProcessingTimeAndSavesPolicy() {
        Policy policy = new Policy();
        policy.setId("policy-002");

        serviceSpy.init(policy);

        ArgumentCaptor<Policy> captor = ArgumentCaptor.forClass(Policy.class);
        verify(policyRepository).save(captor.capture());

        Policy saved = captor.getValue();
        assertNotNull(saved.getMaxProcessingTime());
        assertTrue(saved.getMaxProcessingTime().isAfter(ZonedDateTime.now()));
    }

    @Test
    void testClassifyRiskPolicyHandlesExceptionGracefully() throws InterruptedException {

        Policy policy = new Policy();
        policy.setId("policy-003");

        when(riskAnalysisService.analyzeRisk(policy)).thenThrow(new RuntimeException("Erro simulado"));

        serviceSpy.classifyRiskPolicy(policy);

        Thread.sleep(500); // aguarda execução assíncrona

        // Mesmo com erro, MDC deve ser limpo e não deve lançar exceção
        verify(policyRepository, never()).save(policy);
    }
}

