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
public class AccountBalanceRequest {

    @NotNull(message = "Field cannot be empty!")
    @DecimalMin(value = "0.01")
    private BigDecimal addedAmount;
}
