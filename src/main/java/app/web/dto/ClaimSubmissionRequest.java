package app.web.dto;

import app.claim.model.ClaimType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClaimSubmissionRequest {

    @NotNull(message = "Field cannot be empty!")
    private ClaimType claimType;

    @NotNull(message = "Field cannot be empty!")
    @DecimalMin(value = "0.00")
    private BigDecimal requestedAmount;

    @NotNull(message = "Field cannot be empty!")
    private String attachedDocument;

    private String description;
}
