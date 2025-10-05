package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchPolicyServiceTests {

    @Mock
    private PolicyRepository policyRepository;

    private SearchPolicyService service;

    @BeforeEach
    void setUp() {
        service = new SearchPolicyService(policyRepository);
    }

    @Test
    void testGetPolicyByIdFound() {
        Policy policy = new Policy();
        policy.setId("policy-001");

        when(policyRepository.findById("policy-001")).thenReturn(Optional.of(policy));

        Optional<Policy> result = service.getPolicyById("policy-001");

        assertTrue(result.isPresent());
        assertEquals("policy-001", result.get().getId());
        verify(policyRepository).findById("policy-001");
    }

    @Test
    void testGetPolicyByIdNotFound() {
        when(policyRepository.findById("policy-404")).thenReturn(Optional.empty());

        Optional<Policy> result = service.getPolicyById("policy-404");

        assertTrue(result.isEmpty());
        verify(policyRepository).findById("policy-404");
    }

    @Test
    void testGetPolicyByClientId() {
        Policy policy = new Policy();
        policy.setId("policy-002");

        PageRequest pageable = PageRequest.of(0, 10);
        Page<Policy> page = new PageImpl<>(List.of(policy), pageable, 1);

        when(policyRepository.findByClientId("client-001", pageable)).thenReturn(page);

        Page<Policy> result = service.getPolicyByClientId("client-001", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("policy-002", result.getContent().get(0).getId());
        verify(policyRepository).findByClientId("client-001", pageable);
    }

    @Test
    void testGetPolicies() {
        Policy policy = new Policy();
        policy.setId("policy-003");

        PageRequest pageable = PageRequest.of(0, 10);
        Page<Policy> page = new PageImpl<>(List.of(policy), pageable, 1);

        when(policyRepository.findAll(pageable)).thenReturn(page);

        Page<Policy> result = service.getPolicies(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("policy-003", result.getContent().get(0).getId());
        verify(policyRepository).findAll(pageable);
    }
}
