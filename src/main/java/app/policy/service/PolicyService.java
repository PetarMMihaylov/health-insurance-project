package app.policy.service;

import app.exception.DomainException;
import app.exception.PolicyNotFoundException;
import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.policy.repository.PolicyRepository;
import app.user.model.User;
import app.web.dto.PolicyLimitsChangeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PolicyService {

    private final PolicyRepository policyRepository;

    public PolicyService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    public Policy getByType (PolicyType policyType) {
        return policyRepository.findByPolicyType(policyType);
    }

    public List<Policy> getPolicies() {
        return policyRepository.findAll();
    }

    public Policy getById(UUID id) {
        return policyRepository.findById(id).orElseThrow(() -> new PolicyNotFoundException("Policy with [%s] id is not present.".formatted(id)));
    }

    public void updatePolicyLimits(Policy policy, PolicyLimitsChangeRequest policyLimitsChangeRequest, User admin) {
        policy.setLimitForMedications(policyLimitsChangeRequest.getLimitForMedications());
        policy.setLimitForHospitalTreatment(policyLimitsChangeRequest.getLimitForHospitalTreatment());
        policy.setLimitForSurgery(policyLimitsChangeRequest.getLimitForSurgery());
        policy.setLimitForDentalService(policyLimitsChangeRequest.getLimitForDentalService());
        policy.setPolicyPrice(policyLimitsChangeRequest.getPolicyPrice());
        policy.setUpdatedOn(LocalDateTime.now());

        policyRepository.save(policy);

        log.info("Policy [{}] updated by user [{}].", policy.getPolicyType().getDisplayName(), admin.getUsername());
    }
}
