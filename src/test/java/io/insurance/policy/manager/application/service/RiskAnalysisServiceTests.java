package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.application.service.interfaces.ISearchPolicyService;
import io.insurance.policy.manager.boundaries.driven.http.client.FraudPreventionClient;
import io.insurance.policy.manager.boundaries.driven.http.dto.RiskAnalysisResponse;
import io.insurance.policy.manager.domain.exception.PolicyNotFound;
import io.insurance.policy.manager.domain.model.*;
import io.insurance.policy.manager.domain.model.enums.RiskClassification;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.OccurrencesRepository;
import io.insurance.policy.manager.domain.repository.RiskAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RiskAnalysisServiceTest {

    @Mock
    private ISearchPolicyService searchPolicyService;

    @Mock
    private IPolicyStatusHandler policyStatusHandler;

    @Mock
    private FraudPreventionClient fraudPreventionClient;

    @Mock
    private RiskAnalysisRepository riskAnalysisRepository;

    @Mock
    private OccurrencesRepository occurrencesRepository;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    @InjectMocks
    private RiskAnalysisService riskAnalysisService;

    private Policy policy;
    private RiskAnalysisResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        policy = new Policy();
        policy.setId("POL123");
        policy.setClientId("CLIENT001");
        policy.setProductId(1L);
        policy.setCategory("AUTO");
        policy.setInsuredAmount(50000L);
        policy.setMonthlyPremium(300L);
        policy.setCoverages(Set.of(Coverage.builder().policyId("POL123").typeCoverage("Roubo").insuredAmount(100000L).build(),
                Coverage.builder().policyId("POL123").typeCoverage("Perda Total").insuredAmount(100000L).build()));
        policy.setAssistances(Set.of(Assistances.builder().policyId("POL123").assistance("Guincho até 250km").build()));

        response = new RiskAnalysisResponse(
                    "ORDER123",
                    "CLIENT001",
                    ZonedDateTime.now(),
                    RiskClassification.HIGH_RISK.name(),
                    List.of( new RiskAnalysisResponse.Occurrence(
                                    "OCC001",
                                    1001L,
                                    "FRAUD",
                                    "Suspeita de fraude",
                                    ZonedDateTime.now().minusDays(1),
                                    ZonedDateTime.now())));

    }

    @Test
    void shouldAnalyzeRiskSuccessfully() {
        when(searchPolicyService.getPolicyById("POL123")).thenReturn(Optional.of(policy));
        when(fraudPreventionClient.analyzeRisk(any())).thenReturn(response);
        when(riskAnalysisRepository.save(any())).thenReturn(RiskAnalysis.builder().id(1L).build());

        riskAnalysisService.analyzeRisk("trace-001", "POL123");

        assertEquals(RiskClassification.HIGH_RISK, policy.getRiskClassification());
        verify(policyStatusHandler).processingStatusHandler(policy, Status.VALIDATED, ProcessingStatus.AWAITING_RULES_VALIDATION);
        verify(riskAnalysisRepository).save(any());
        verify(occurrencesRepository).save(any());
        verify(metricsCollector).incrementRiskAnalysis(RiskClassification.HIGH_RISK.name().toLowerCase());
    }

    @Test
    void shouldThrowPolicyNotFoundException() {
        when(searchPolicyService.getPolicyById("POL404")).thenReturn(Optional.empty());

        assertThrows(PolicyNotFound.class, () -> riskAnalysisService.analyzeRisk("trace-002", "POL404"));
    }

    @Test
    void shouldHandleExceptionDuringPersistenceGracefully() {
        when(searchPolicyService.getPolicyById("POL123")).thenReturn(Optional.of(policy));
        when(fraudPreventionClient.analyzeRisk(any())).thenReturn(response);
        when(riskAnalysisRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertDoesNotThrow(() -> riskAnalysisService.analyzeRisk("trace-003", "POL123"));

        verify(metricsCollector).incrementRiskAnalysis(RiskClassification.HIGH_RISK.name().toLowerCase());
    }
}
