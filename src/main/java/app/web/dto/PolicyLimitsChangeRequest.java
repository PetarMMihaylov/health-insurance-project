package app.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PolicyLimitsChangeRequest {

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal limitForMedications;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal limitForHospitalTreatment;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal limitForSurgery;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal limitForDentalService;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal policyPrice;
}
