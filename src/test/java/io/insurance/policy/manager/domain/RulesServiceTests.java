package io.insurance.policy.manager.domain;

import io.insurance.policy.manager.application.service.BusinessMetricsCollector;
import io.insurance.policy.manager.application.service.ProcessingStatus;
import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.application.service.interfaces.ISearchPolicyService;
import io.insurance.policy.manager.domain.exception.PolicyNotFound;
import io.insurance.policy.manager.domain.exception.RiskClassificationException;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.Rules;
import io.insurance.policy.manager.domain.model.enums.RiskClassification;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.RulesRepository;
import io.insurance.policy.manager.domain.rules.RulesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RulesServiceTest {

    @Mock
    private ISearchPolicyService searchPolicyService;

    @Mock
    private IPolicyStatusHandler policyStatusHandler;

    @Mock
    private RulesRepository rulesRepository;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    @InjectMocks
    private RulesService rulesService;

    private Policy samplePolicy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        samplePolicy = new Policy();
        samplePolicy.setId("POL123");
        samplePolicy.setRiskClassification(RiskClassification.HIGH_RISK);
        samplePolicy.setCategory("AUTO");
        samplePolicy.setInsuredAmount(50000L);
        samplePolicy.setCreatedAt(ZonedDateTime.now().minusHours(2));
        samplePolicy.setStatus(Status.REJECTED); // para testar métricas
    }

    @Test
    void shouldApplyRulesSuccessfully() {
        Rules rule = new Rules(1L, "AUTO", RiskClassification.HIGH_RISK, 60000L);
        when(searchPolicyService.getPolicyById("POL123")).thenReturn(Optional.of(samplePolicy));
        when(rulesRepository.findByRiskClassification(RiskClassification.HIGH_RISK)).thenReturn(List.of(rule));

        rulesService.applyRules("trace-001", "POL123");

        verify(policyStatusHandler).processingStatusHandler(eq(samplePolicy), eq(Status.PENDING), eq(ProcessingStatus.AWAITING_PAYMENT_AND_SUBSCRIPTION));
        verify(metricsCollector).incrementPoliciesRejected();
        verify(metricsCollector).recordPolicyProcessingTime(any());
    }

    @Test
    void shouldRejectPolicyIfNoMatchingRules() {
        Rules rule = new Rules(1L, "LIFE", RiskClassification.HIGH_RISK,  10000L); // categoria diferente
        when(searchPolicyService.getPolicyById("POL123")).thenReturn(Optional.of(samplePolicy));
        when(rulesRepository.findByRiskClassification(RiskClassification.HIGH_RISK)).thenReturn(List.of(rule));

        rulesService.applyRules("trace-002", "POL123");

        verify(policyStatusHandler).processingStatusHandler(eq(samplePolicy), eq(Status.REJECTED), eq(ProcessingStatus.COMPLETED));
        assertNotNull(samplePolicy.getFinishedAt());
    }

    @Test
    void shouldThrowPolicyNotFoundException() {
        when(searchPolicyService.getPolicyById("POL404")).thenReturn(Optional.empty());

        assertThrows(PolicyNotFound.class, () -> rulesService.applyRules("trace-003", "POL404"));
    }

    @Test
    void shouldThrowRiskClassificationException() {
        samplePolicy.setRiskClassification(null);
        when(searchPolicyService.getPolicyById("POL123")).thenReturn(Optional.of(samplePolicy));

        assertThrows(RiskClassificationException.class, () -> rulesService.applyRules("trace-004", "POL123"));
    }
}