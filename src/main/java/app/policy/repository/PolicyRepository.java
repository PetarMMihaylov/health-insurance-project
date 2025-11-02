package app.policy.repository;

import app.policy.model.Policy;
import app.policy.model.PolicyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, UUID> {
    Policy findByPolicyType(PolicyType policyType);
}
