package app.report.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSummaryRequest {

    private UUID userId;

    private LocalDate startDate;
    private LocalDate endDate;

    private int totalClaims;
    private int totalApprovedClaims;
    private BigDecimal totalReimbursedAmount;
    private int totalTransactions;
}
