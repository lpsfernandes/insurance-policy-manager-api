package io.insurance.policy.manager.application.service;
import io.insurance.policy.manager.boundaries.driving.http.dto.PolicyRequest;
import io.insurance.policy.manager.boundaries.driving.http.dto.enums.Category;
import io.insurance.policy.manager.boundaries.driving.http.dto.enums.PaymentMethod;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.AssistancesRepository;
import io.insurance.policy.manager.domain.repository.CoverageRepository;
import io.insurance.policy.manager.domain.repository.PolicyRepository;
import io.insurance.policy.manager.domain.repository.StatusHistoryRepository;
import io.insurance.policy.manager.boundaries.driving.http.dto.enums.SalesChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class CreatePolicyServiceTests {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private CoverageRepository coverageRepository;

    @Mock
    private AssistancesRepository assistancesRepository;

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    private CreatePolicyService serviceSpy;

    private PolicyRequest request;

    private Policy savedPolicy;

    @BeforeEach
    void setUp() {
        CreatePolicyService service = new CreatePolicyService(
                policyRepository,
                coverageRepository,
                assistancesRepository,
                statusHistoryRepository,
                metricsCollector
        );
        serviceSpy = Mockito.spy(service);

        request = new PolicyRequest(
                "client-123",
                456L,
                Category.AUTO,
                SalesChannel.WEB_SITE,
                PaymentMethod.CREDIT_CARD,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(100),
                Map.of("ROUBO", BigDecimal.valueOf(5000)),
                Set.of("GUINCHO", "CHAVEIRO")
        );

        savedPolicy = Policy.builder()
                .id("policy-789")
                .clientId(request.clientId())
                .productId(request.productId())
                .category(request.category().name())
                .paymentMethod(request.paymentMethod().name())
                .salesChannel(request.salesChannel().name())
                .insuredAmount(100000L)
                .monthlyPremium(10000L)
                .status(Status.RECEIVED)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();
    }

    @Test
    void testCreatePolicySuccess() {
        when(this.policyRepository.save(any())).thenReturn(savedPolicy);
        doNothing().when(serviceSpy).insertOutboxEvent(any());

        Optional<Policy> result = serviceSpy.createPolicy(request);

        assertTrue(result.isPresent());
        assertEquals(savedPolicy.getId(), result.get().getId());
        assertEquals(ProcessingStatus.AWAITING_RISK_ANALYSIS, savedPolicy.getProcessingStatus());

        verify(serviceSpy).insertPolicy(any(), eq(request));
        verify(policyRepository, times(1)).save(any());
        verify(assistancesRepository, times(request.assistances().size())).save(any());
        verify(coverageRepository, times(request.coverages().size())).save(any());
        verify(statusHistoryRepository, times(1)).save(any());
        verify(serviceSpy).insertOutboxEvent(savedPolicy);
        verify(metricsCollector).incrementPoliciesCreated();
    }

    @Test
    void testCreatePolicyFailure() {
        doThrow(new RuntimeException("Erro simulado")).when(serviceSpy).insertPolicy(any(), any());

        Optional<Policy> result = serviceSpy.createPolicy(request);

        assertTrue(result.isEmpty());
        verify(serviceSpy).insertPolicy(any(), any());
        verify(metricsCollector, never()).incrementPoliciesCreated();
    }

    @Test
    void testInsertAssistancesShouldSaveEachAssistance() {
        serviceSpy.insertAssistances("policy-789", Set.of("GUINCHO", "CHAVEIRO"));

        verify(assistancesRepository).save(argThat(a -> a.getAssistance().equals("GUINCHO")));
        verify(assistancesRepository).save(argThat(a -> a.getAssistance().equals("CHAVEIRO")));
    }

    @Test
    void testInsertCoveragesShouldSaveEachCoverage() {
        Map<String, BigDecimal> coverages = Map.of("ROUBO", BigDecimal.valueOf(5000));
        serviceSpy.insertCoverages("policy-789", coverages);

        verify(coverageRepository).save(argThat(c ->
                c.getTypeCoverage().equals("ROUBO") &&
                        c.getInsuredAmount() == 500000L
        ));
    }

    @Test
    void testInsertStatusHistoryShouldSaveCorrectly() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        serviceSpy.insertStatusHistory("policy-789", Status.RECEIVED, now);

        verify(statusHistoryRepository).save(argThat(s ->
                s.getPolicyId().equals("policy-789") &&
                        s.getStatus() == Status.RECEIVED &&
                        s.getCreatedAt().equals(now)
        ));
    }
}