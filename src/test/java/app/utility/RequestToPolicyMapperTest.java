package app.utility;

import app.policy.model.Policy;
import app.web.dto.PolicyLimitsChangeRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RequestToPolicyMapperTest {

    @Test
    void fromPolicyToEditRequest_mapsAllFieldsCorrectly() {

        Policy policy = Policy.builder()
                .limitForMedications(BigDecimal.valueOf(500))
                .limitForHospitalTreatment(BigDecimal.valueOf(1500))
                .limitForSurgery(BigDecimal.valueOf(2500))
                .limitForDentalService(BigDecimal.valueOf(300))
                .policyPrice(BigDecimal.valueOf(49.99))
                .build();

        PolicyLimitsChangeRequest request = RequestToPolicyMapper.fromPolicyToEditRequest(policy);

        assertThat(request.getLimitForMedications()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(request.getLimitForHospitalTreatment()).isEqualByComparingTo(BigDecimal.valueOf(1500));
        assertThat(request.getLimitForSurgery()).isEqualByComparingTo(BigDecimal.valueOf(2500));
        assertThat(request.getLimitForDentalService()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(request.getPolicyPrice()).isEqualByComparingTo(BigDecimal.valueOf(49.99));
    }

    @Test
    void fromPolicyToEditRequest_withNullPolicy_returnsRequestWithNullFields() {

        Policy policy = Policy.builder()
                .limitForMedications(null)
                .limitForHospitalTreatment(null)
                .limitForSurgery(null)
                .limitForDentalService(null)
                .policyPrice(null)
                .build();

        PolicyLimitsChangeRequest request = RequestToPolicyMapper.fromPolicyToEditRequest(policy);

        assertThat(request.getLimitForMedications()).isNull();
        assertThat(request.getLimitForHospitalTreatment()).isNull();
        assertThat(request.getLimitForSurgery()).isNull();
        assertThat(request.getLimitForDentalService()).isNull();
        assertThat(request.getPolicyPrice()).isNull();
    }
}
