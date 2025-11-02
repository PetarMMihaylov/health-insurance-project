package app.web.dto;

import app.claim.model.ClaimType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClaimSubmissionRequest {

    @NotNull
    private ClaimType claimType;

    @NotNull
    private BigDecimal requestedAmount;

    @NotNull
    private String attachedDocument;

    private String description;
}
