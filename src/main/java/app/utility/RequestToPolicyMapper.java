package app.utility;

import app.policy.model.Policy;
import app.web.dto.PolicyLimitsChangeRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestToPolicyMapper {
    public static PolicyLimitsChangeRequest fromPolicyToEditRequest(Policy policy) {

        return PolicyLimitsChangeRequest.builder()
                .limitForMedications(policy.getLimitForMedications())
                .limitForHospitalTreatment(policy.getLimitForHospitalTreatment())
                .limitForSurgery(policy.getLimitForSurgery())
                .limitForDentalService(policy.getLimitForDentalService())
                .policyPrice(policy.getPolicyPrice())
                .build();
    }
}