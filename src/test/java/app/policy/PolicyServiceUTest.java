package app.policy;

import app.exception.PolicyNotFoundException;
import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.policy.repository.PolicyRepository;
import app.policy.service.PolicyService;
import app.user.model.User;
import app.web.dto.PolicyLimitsChangeRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PolicyServiceUTest {

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private PolicyService policyService;

    @Test
    void whenGetByType_andPolicyExists_thenReturnPolicy() {

        PolicyType type = PolicyType.COMFORT;

        Policy policy = Policy.builder()
                .id(UUID.randomUUID())
                .policyType(type)
                .limitForMedications(BigDecimal.valueOf(500))
                .limitForHospitalTreatment(BigDecimal.valueOf(1500))
                .limitForSurgery(BigDecimal.valueOf(2500))
                .limitForDentalService(BigDecimal.valueOf(300))
                .policyPrice(BigDecimal.valueOf(49.99))
                .createdOn(LocalDateTime.now().minusDays(10))
                .updatedOn(LocalDateTime.now().minusDays(1))
                .build();

        when(policyRepository.findByPolicyType(type)).thenReturn(policy);

        Policy result = policyService.getByType(type);

        assertNotNull(result);
        assertEquals(PolicyType.COMFORT, result.getPolicyType());
        assertEquals(BigDecimal.valueOf(500), result.getLimitForMedications());
        assertEquals(BigDecimal.valueOf(1500), result.getLimitForHospitalTreatment());
        assertEquals(BigDecimal.valueOf(2500), result.getLimitForSurgery());
        assertEquals(BigDecimal.valueOf(300), result.getLimitForDentalService());
        assertEquals(BigDecimal.valueOf(49.99), result.getPolicyPrice());

        verify(policyRepository).findByPolicyType(type);

    }

    @Test
    void whenGetByType_andPolicyDoesNotExist_thenReturnNull() {

        PolicyType type = PolicyType.LUX;
        when(policyRepository.findByPolicyType(type)).thenReturn(null);

        Policy result = policyService.getByType(type);

        assertNull(result);

        verify(policyRepository).findByPolicyType(type);
    }

    @Test
    void whenGetPolicies_andPoliciesExist_thenReturnFullList() {

        Policy policy1 = Policy.builder()
                .id(UUID.randomUUID())
                .policyType(PolicyType.STANDARD)
                .limitForMedications(BigDecimal.valueOf(300))
                .limitForHospitalTreatment(BigDecimal.valueOf(1000))
                .limitForSurgery(BigDecimal.valueOf(2000))
                .limitForDentalService(BigDecimal.valueOf(200))
                .policyPrice(BigDecimal.valueOf(29.99))
                .createdOn(LocalDateTime.now().minusDays(5))
                .updatedOn(LocalDateTime.now().minusDays(2))
                .build();

        Policy policy2 = Policy.builder()
                .id(UUID.randomUUID())
                .policyType(PolicyType.LUX)
                .limitForMedications(BigDecimal.valueOf(1000))
                .limitForHospitalTreatment(BigDecimal.valueOf(5000))
                .limitForSurgery(BigDecimal.valueOf(7000))
                .limitForDentalService(BigDecimal.valueOf(500))
                .policyPrice(BigDecimal.valueOf(99.99))
                .createdOn(LocalDateTime.now().minusDays(20))
                .updatedOn(LocalDateTime.now().minusDays(10))
                .build();

        when(policyRepository.findAll()).thenReturn(List.of(policy1, policy2));

        List<Policy> result = policyService.getPolicies();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(policy1, policy2);
        assertEquals(PolicyType.STANDARD, result.get(0).getPolicyType());
        assertEquals(PolicyType.LUX, result.get(1).getPolicyType());

        verify(policyRepository).findAll();
    }

    @Test
    void whenGetPolicies_andNoPoliciesExist_thenReturnEmptyList() {

        when(policyRepository.findAll()).thenReturn(List.of());

        List<Policy> result = policyService.getPolicies();

        assertThat(result).isEmpty();

        verify(policyRepository).findAll();
    }

    @Test
    void whenGetById_andPolicyExists_thenReturnPolicy() {

        UUID policyId = UUID.randomUUID();
        Policy policy = Policy.builder()
                .id(policyId)
                .policyType(PolicyType.COMFORT)
                .limitForMedications(BigDecimal.valueOf(500))
                .limitForHospitalTreatment(BigDecimal.valueOf(1500))
                .limitForSurgery(BigDecimal.valueOf(2500))
                .limitForDentalService(BigDecimal.valueOf(300))
                .policyPrice(BigDecimal.valueOf(49.99))
                .createdOn(LocalDateTime.now().minusDays(3))
                .updatedOn(LocalDateTime.now().minusDays(1))
                .build();

        when(policyRepository.findById(policyId)).thenReturn(Optional.of(policy));

        Policy result = policyService.getById(policyId);

        assertNotNull(result);
        assertEquals(policyId, result.getId());
        assertEquals(PolicyType.COMFORT, result.getPolicyType());
        assertEquals(BigDecimal.valueOf(500), result.getLimitForMedications());
        assertEquals(BigDecimal.valueOf(1500), result.getLimitForHospitalTreatment());
        assertEquals(BigDecimal.valueOf(2500), result.getLimitForSurgery());
        assertEquals(BigDecimal.valueOf(300), result.getLimitForDentalService());
        assertEquals(BigDecimal.valueOf(49.99), result.getPolicyPrice());

        verify(policyRepository).findById(policyId);
    }

    @Test
    void whenGetById_andPolicyDoesNotExist_thenThrowPolicyNotFoundException() {

        UUID policyId = UUID.randomUUID();
        when(policyRepository.findById(policyId)).thenReturn(Optional.empty());

        PolicyNotFoundException exception = assertThrows(
                PolicyNotFoundException.class,
                () -> policyService.getById(policyId)
        );

        assertEquals("Policy with [" + policyId + "] id is not present.", exception.getMessage());

        verify(policyRepository).findById(policyId);
    }

    @Test
    void whenUpdatePolicyLimits_andValidRequest_thenAllFieldsAreUpdated_andPolicyIsSaved() {

        Policy policy = Policy.builder()
                .id(UUID.randomUUID())
                .policyType(PolicyType.STANDARD)
                .limitForMedications(BigDecimal.valueOf(200))
                .limitForHospitalTreatment(BigDecimal.valueOf(800))
                .limitForSurgery(BigDecimal.valueOf(1500))
                .limitForDentalService(BigDecimal.valueOf(100))
                .policyPrice(BigDecimal.valueOf(19.99))
                .createdOn(LocalDateTime.now().minusDays(10))
                .updatedOn(LocalDateTime.now().minusDays(5))
                .build();

        PolicyLimitsChangeRequest request = PolicyLimitsChangeRequest.builder()
                .limitForMedications(BigDecimal.valueOf(500))
                .limitForHospitalTreatment(BigDecimal.valueOf(1200))
                .limitForSurgery(BigDecimal.valueOf(1800))
                .limitForDentalService(BigDecimal.valueOf(300))
                .policyPrice(BigDecimal.valueOf(49.99))
                .build();

        User admin = User.builder()
                .username("admin_user")
                .build();

        policyService.updatePolicyLimits(policy, request, admin);

        assertEquals(BigDecimal.valueOf(500), policy.getLimitForMedications());
        assertEquals(BigDecimal.valueOf(1200), policy.getLimitForHospitalTreatment());
        assertEquals(BigDecimal.valueOf(1800), policy.getLimitForSurgery());
        assertEquals(BigDecimal.valueOf(300), policy.getLimitForDentalService());
        assertEquals(BigDecimal.valueOf(49.99), policy.getPolicyPrice());

        assertThat(policy.getUpdatedOn())
                .isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

        verify(policyRepository).save(policy);
    }
}
