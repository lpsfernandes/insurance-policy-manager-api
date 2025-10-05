package io.insurance.policy.manager.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.insurance.policy.manager.application.controller.dto.PolicyRequest;
import io.insurance.policy.manager.application.controller.dto.UpdatePoliceRequest;
import io.insurance.policy.manager.application.controller.dto.enums.Category;
import io.insurance.policy.manager.application.controller.dto.enums.PaymentMethod;
import io.insurance.policy.manager.application.controller.dto.enums.SalesChannel;
import io.insurance.policy.manager.application.service.interfaces.ICreatePolicyService;
import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.application.service.interfaces.ISearchPolicyService;
import io.insurance.policy.manager.domain.exception.StatusNotAllowed;
import io.insurance.policy.manager.domain.model.Assistances;
import io.insurance.policy.manager.domain.model.Coverage;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.StatusHistory;
import io.insurance.policy.manager.domain.model.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PolicyControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICreatePolicyService createPolicyService;

    @MockBean
    private ISearchPolicyService searchPolicyService;

    @MockBean
    private IPolicyStatusHandler policyStatusHandler;

    @Test
    void testCreatePolicySuccess() throws Exception {
        PolicyRequest request = new PolicyRequest(
                "client-001",
                123L,
                Category.AUTO,
                SalesChannel.WEB_SITE,
                PaymentMethod.CREDIT_CARD,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(100),
                Map.of("ROUBO", BigDecimal.valueOf(5000)),
                Set.of("GUINCHO", "CHAVEIRO")
        );

        when(createPolicyService.createPolicy(any())).thenReturn(Optional.of(Policy.builder()
                .id("policy-001")
                .clientId("client-123")
                .productId(1L)
                .category("LIFE")
                .paymentMethod(PaymentMethod.PIX.name())
                .salesChannel(SalesChannel.WHATSAPP.name())
                .insuredAmount(100000L)
                .monthlyPremium(10000L)
                .status(Status.RECEIVED)
                .assistances(Set.of(Assistances.builder().assistance("GUINCHO").build()))
                .history(Set.of(StatusHistory.builder().status(Status.RECEIVED).createdAt(ZonedDateTime.now(ZoneId.of("UTC"))).build()))
                .coverages(Set.of(Coverage.builder().typeCoverage("ROUBO").insuredAmount(100000L).build()))
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build()));

        mockMvc.perform(post("/api/v1/policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("policy-001"));
    }

    @Test
    void testCreatePolicyFailure() throws Exception {
        PolicyRequest request = new PolicyRequest(
                "client-001", 123L, Category.AUTO, SalesChannel.WEB_SITE,
                PaymentMethod.CREDIT_CARD, BigDecimal.valueOf(1000), BigDecimal.valueOf(100),
                Map.of(), Set.of()
        );

        when(createPolicyService.createPolicy(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetPolicySuccess() throws Exception {
        when(searchPolicyService.getPolicyById("policy-001"))
                .thenReturn(Optional.of(Policy.builder()
                        .id("policy-001")
                        .clientId("client-123")
                        .productId(1L)
                        .category("LIFE")
                        .paymentMethod(PaymentMethod.PIX.name())
                        .salesChannel(SalesChannel.WHATSAPP.name())
                        .insuredAmount(100000L)
                        .monthlyPremium(10000L)
                        .status(Status.RECEIVED)
                        .assistances(Set.of(Assistances.builder().assistance("GUINCHO").build()))
                        .history(Set.of(StatusHistory.builder().status(Status.RECEIVED).createdAt(ZonedDateTime.now(ZoneId.of("UTC"))).build()))
                        .coverages(Set.of(Coverage.builder().typeCoverage("ROUBO").insuredAmount(100000L).build()))
                        .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                        .build()));

        mockMvc.perform(get("/api/v1/policy/policy-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("policy-001"));
    }

    @Test
    void testGetPolicyNotFound() throws Exception {
        when(searchPolicyService.getPolicyById("policy-404")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/policy/policy-404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPoliciesByClientId() throws Exception {
        var response = Policy.builder()
                        .id("policy-001")
                        .clientId("client-123")
                        .productId(1L)
                        .category("LIFE")
                        .paymentMethod(PaymentMethod.PIX.name())
                        .salesChannel(SalesChannel.WHATSAPP.name())
                        .insuredAmount(100000L)
                        .monthlyPremium(10000L)
                        .status(Status.RECEIVED)
                        .assistances(Set.of(Assistances.builder().assistance("GUINCHO").build()))
                        .history(Set.of(StatusHistory.builder().status(Status.RECEIVED).createdAt(ZonedDateTime.now(ZoneId.of("UTC"))).build()))
                        .coverages(Set.of(Coverage.builder().typeCoverage("ROUBO").insuredAmount(100000L).build()))
                        .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                        .build();


        Page<Policy> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(searchPolicyService.getPolicyByClientId(eq("client-001"), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/policy")
                        .param("clientId", "client-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("policy-001"));
    }

    @Test
    void testUpdatePolicyStatusSuccess() throws Exception {
        UpdatePoliceRequest request = new UpdatePoliceRequest(Status.APPROVED);

        when(policyStatusHandler.statusHandler(eq("policy-001"), eq(Status.APPROVED)))
                .thenReturn(Optional.of(Policy.builder()
                        .id("policy-001")
                        .clientId("client-123")
                        .productId(1L)
                        .category("LIFE")
                        .paymentMethod(PaymentMethod.PIX.name())
                        .salesChannel(SalesChannel.WHATSAPP.name())
                        .insuredAmount(100000L)
                        .monthlyPremium(10000L)
                        .status(Status.RECEIVED)
                        .assistances(Set.of(Assistances.builder().assistance("GUINCHO").build()))
                        .history(Set.of(StatusHistory.builder().status(Status.RECEIVED).createdAt(ZonedDateTime.now(ZoneId.of("UTC"))).build()))
                        .coverages(Set.of(Coverage.builder().typeCoverage("ROUBO").insuredAmount(100000L).build()))
                        .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                        .build()));

        mockMvc.perform(patch("/api/v1/policy/policy-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("policy-001"));
    }

    @Test
    void testUpdatePolicyStatusNotFound() throws Exception {
        UpdatePoliceRequest request = new UpdatePoliceRequest(Status.CANCELED);

        when(policyStatusHandler.statusHandler(eq("policy-404"), eq(Status.CANCELED)))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/v1/policy/policy-404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdatePolicyStatusUnprocessableEntity() throws Exception {
        UpdatePoliceRequest request = new UpdatePoliceRequest(Status.CANCELED);

        when(policyStatusHandler.statusHandler(eq("policy-404"), eq(Status.CANCELED)))
                .thenThrow(new StatusNotAllowed(Status.CANCELED, Status.APPROVED));

        mockMvc.perform(patch("/api/v1/policy/policy-404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

}
