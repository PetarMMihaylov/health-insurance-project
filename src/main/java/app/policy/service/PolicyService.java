package app.policy.service;

import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.policy.repository.PolicyRepository;
import org.springframework.stereotype.Service;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;

    public PolicyService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    public Policy getByType (PolicyType policyType) {
        return policyRepository.findByPolicyType(policyType);
    }
}
