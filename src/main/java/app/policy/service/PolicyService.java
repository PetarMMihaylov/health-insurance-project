package app.policy.service;

import app.exception.DomainException;
import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.policy.repository.PolicyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
        return policyRepository.findById(id).orElseThrow(() -> new DomainException("Policy with [%s] id is not present.".formatted(id)));
    }
}
