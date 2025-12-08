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

    @NotNull(message = "Field cannot be empty!")
    @DecimalMin(value = "0.00")
    private BigDecimal limitForMedications;

    @NotNull(message = "Field cannot be empty!")
    @DecimalMin(value = "0.00")
    private BigDecimal limitForHospitalTreatment;

    @NotNull(message = "Field cannot be empty!")
    @DecimalMin(value = "0.00")
    private BigDecimal limitForSurgery;

    @NotNull(message = "Field cannot be empty!")
    @DecimalMin(value = "0.00")
    private BigDecimal limitForDentalService;

    @NotNull(message = "Field cannot be empty!")
    @DecimalMin(value = "0.00")
    private BigDecimal policyPrice;
}
