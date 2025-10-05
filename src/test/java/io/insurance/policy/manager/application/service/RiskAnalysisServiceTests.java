package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.application.controller.dto.enums.Category;
import io.insurance.policy.manager.boundaries.driving.http.client.FraudPreventionClient;
import io.insurance.policy.manager.boundaries.driving.http.dto.RiskAnalysisResponse;
import io.insurance.policy.manager.domain.model.Assistances;
import io.insurance.policy.manager.domain.model.Coverage;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.RiskAnalysis;
import io.insurance.policy.manager.domain.model.enums.RiskClassification;
import io.insurance.policy.manager.domain.repository.OccurrencesRepository;
import io.insurance.policy.manager.domain.repository.RiskAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RiskAnalysisServiceTests {

    @Mock
    private FraudPreventionClient fraudPreventionClient;

    @Mock
    private RiskAnalysisRepository riskAnalysisRepository;

    @Mock
    private OccurrencesRepository occurrencesRepository;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    private RiskAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new RiskAnalysisService(fraudPreventionClient, riskAnalysisRepository, occurrencesRepository, metricsCollector);
    }

    @Test
    void testAnalyzeRiskSuccess() {
        Policy policy = new Policy();
        policy.setId("policy-001");
        policy.setClientId("client-001");
        policy.setProductId(123L);
        policy.setCategory(Category.AUTO.name());
        policy.setMonthlyPremium(100L);
        policy.setInsuredAmount(50000L);
        policy.setCoverages(Set.of(Coverage.builder().typeCoverage("ROUBO").insuredAmount(10000L).build()));
        policy.setAssistances(Set.of(Assistances.builder().assistance("GUINCHO").build()));

        RiskAnalysisResponse response = new RiskAnalysisResponse(
                "order-001",
                "client-001",
                ZonedDateTime.now(),
                RiskClassification.HIGH_RISK.name(),
                List.of()
        );

        when(fraudPreventionClient.analyzeRisk(any())).thenReturn(response);
        when(riskAnalysisRepository.save(any())).thenReturn(RiskAnalysis.builder().id(1L).build());

        RiskClassification result = service.analyzeRisk(policy);

        assertEquals(RiskClassification.HIGH_RISK, result);
        verify(metricsCollector).incrementRiskAnalysis("high_risk");
        verify(riskAnalysisRepository).save(any());
    }

    @Test
    void testSaveAnalyzeRiskWithOccurrences() {
        RiskAnalysisResponse response = new RiskAnalysisResponse(
                "order-002",
                "client-002",
                ZonedDateTime.now(),
                RiskClassification.HIGH_RISK.name(),
                List.of(new RiskAnalysisResponse.Occurrence("occ-001", 123L, "FRAUDE", "Suspeita", ZonedDateTime.now(), ZonedDateTime.now()))
        );

        when(riskAnalysisRepository.save(any())).thenReturn(RiskAnalysis.builder().id(2L).build());

        service.saveAnalyzeRisk("policy-002", response);

        verify(riskAnalysisRepository).save(any());
        verify(occurrencesRepository).save(any());
    }

    @Test
    void testSaveAnalyzeRiskHandlesException() {
        RiskAnalysisResponse response = new RiskAnalysisResponse(
                "order-003",
                "client-003",
                ZonedDateTime.now(),
                RiskClassification.LOW_RISK.name(),
                List.of()
        );

        when(riskAnalysisRepository.save(any())).thenThrow(new RuntimeException("Erro simulado"));

        assertDoesNotThrow(() -> service.saveAnalyzeRisk("policy-003", response));

        verify(riskAnalysisRepository).save(any());
        verifyNoInteractions(occurrencesRepository);
    }
}
