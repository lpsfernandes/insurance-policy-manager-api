package io.insurance.policy.manager.domain;

import io.insurance.policy.manager.application.service.BusinessMetricsCollector;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.Rules;
import io.insurance.policy.manager.domain.model.enums.RiskClassification;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.PolicyRepository;
import io.insurance.policy.manager.domain.repository.RulesRepository;
import io.insurance.policy.manager.domain.rules.RulesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


@ExtendWith(MockitoExtension.class)
class RulesServiceTests {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private RulesRepository rulesRepository;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    private RulesService rulesServiceSpy;

    private Policy policy;

    @BeforeEach
    void setUp() {
        Duration expired = Duration.ofMinutes(1);
        RulesService rulesService = new RulesService(expired, policyRepository, rulesRepository, metricsCollector);
        rulesServiceSpy = Mockito.spy(rulesService);

        policy = Policy.builder()
                .id("POL123")
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(5))
                .status(Status.VALIDATED)
                .riskClassification(RiskClassification.HIGH_RISK)
                .category("AUTO")
                .insuredAmount(50000L)
                .build();
    }

    @Test
    void testAnalyzeShouldInvokeInitAndApplyRules() {
        List<Policy> policies = List.of(policy);
        Pageable pageable = PageRequest.of(0, 1000, Sort.Direction.ASC, "createdAt");

        when(policyRepository.findByPoliciesPendingProcessing(eq(Status.VALIDATED), any(), eq(pageable)))
                .thenReturn(policies);

        doNothing().when(rulesServiceSpy).init(any());
        doNothing().when(rulesServiceSpy).applyRules(any());

        rulesServiceSpy.analyze();

        verify(rulesServiceSpy).init(policy);
        verify(rulesServiceSpy).applyRules(policy);
    }

    @Test
    void testApplyRulesShouldInvokeCollectMetricsAndUpdateDatabase() {
        Rules rule = new Rules();
        rule.setCategory("AUTO");
        rule.setInsuredAmountLimit(100000L);

        when(rulesRepository.findByRiskClassification(RiskClassification.HIGH_RISK)).thenReturn(List.of(rule));
        doNothing().when(rulesServiceSpy).collectMetrics(any());
        doNothing().when(rulesServiceSpy).updateDatabaseAndInsertOutboxEventAndInsertStatusHistory(any());

        rulesServiceSpy.applyRules(policy);

        assertEquals(Status.PENDING, policy.getStatus());
        verify(rulesServiceSpy).collectMetrics(policy);
        verify(rulesServiceSpy).updateDatabaseAndInsertOutboxEventAndInsertStatusHistory(policy);
    }

    @Test
    void testCollectMetricsShouldBeCalledWhenRejected() {
        policy.setStatus(Status.REJECTED);
        policy.setFinishedAt(ZonedDateTime.now(ZoneId.of("UTC")));

        rulesServiceSpy.collectMetrics(policy);

        verify(metricsCollector).incrementPoliciesRejected();
        verify(metricsCollector).recordPolicyProcessingTime(any());
    }

    @Test
    void testInitShouldSetMaxProcessingTimeAndSavePolicy() {
        rulesServiceSpy.init(policy);

        assertNotNull(policy.getMaxProcessingTime());
        verify(policyRepository).save(policy);
    }
}