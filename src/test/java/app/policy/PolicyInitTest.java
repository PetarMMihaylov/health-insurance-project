package app.policy;

import app.policy.model.Policy;
import app.policy.model.PolicyInit;
import app.policy.model.PolicyType;
import app.policy.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyInitTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private PolicyInit policyInit;

    @Test
    void whenRepositoryIsEmpty_thenSaveAllIsCalledWithThreePolicies() throws Exception {

        when(policyRepository.count()).thenReturn(0L);

        policyInit.run(applicationArguments);

        ArgumentCaptor<List<Policy>> captor = ArgumentCaptor.forClass(List.class);
        verify(policyRepository).saveAll(captor.capture());

        List<Policy> savedPolicies = captor.getValue();
        assertThat(savedPolicies).hasSize(3);

        assertThat(savedPolicies)
                .extracting(Policy::getPolicyType)
                .containsExactlyInAnyOrder(PolicyType.COMFORT, PolicyType.STANDARD, PolicyType.LUX);

        Policy comfortPolicy = savedPolicies.stream()
                .filter(p -> p.getPolicyType() == PolicyType.COMFORT)
                .findFirst()
                .orElseThrow();

        assertThat(comfortPolicy.getLimitForMedications()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(comfortPolicy.getPolicyPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void whenRepositoryIsNotEmpty_thenSaveAllIsNotCalled() throws Exception {

        when(policyRepository.count()).thenReturn(3L);

        policyInit.run(applicationArguments);

        verify(policyRepository, never()).saveAll(any());
    }
}
