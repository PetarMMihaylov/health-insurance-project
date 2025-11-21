package app.report.service;

import app.claim.model.ClaimStatus;
import app.claim.service.ClaimService;
import app.report.client.ReportClient;
import app.report.client.dto.CreateSummaryRequest;
import app.report.client.dto.Summary;
import app.transaction.model.Transaction;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.web.dto.CreateSummaryByDates;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ReportService {

    private final ReportClient client;
    private final ClaimService claimService;
    private final TransactionService transactionService;

    @Autowired
    public ReportService(ReportClient client, ClaimService claimService, TransactionService transactionService) {
        this.client = client;
        this.claimService = claimService;
        this.transactionService = transactionService;
    }

    public List<Summary> getLastUserReports(UUID userId) {

        ResponseEntity<List<Summary>> response = client.getReports(userId);

        return response.getBody() != null
                ? response.getBody().stream().limit(10).toList()
                : Collections.emptyList();
    }

    public void deleteReport(UUID id, UUID userId) {

        try {
            Summary summary = client.getReportDetails(id).getBody();

            if (!summary.getUserId().equals(userId)) {
                throw new SecurityException("Cannot delete report of another user.");
            }

            client.deleteReport(id);
        } catch (FeignException exception) {
            log.error("[Failed]: Reason for failure: %s.".formatted(exception.getMessage()));
            throw exception;
        }
    }

    public Summary getReportById(UUID id, UUID userId) {

        Summary summary;

        try {
            summary = client.getReportDetails(id).getBody();
        } catch (FeignException exception) {
            log.error("[Failed]: Reason for failure: %s.".formatted(exception.getMessage()));
            throw exception;
        }

        if (!summary.getUserId().equals(userId)) {
            throw new SecurityException("Access denied: This report does not belong to you.");
        }

        return summary;
    }

    public void createReport(CreateSummaryByDates createSummaryByDates, User user) {

        LocalDate startDate = createSummaryByDates.getStartDate();
        LocalDate endDate = createSummaryByDates.getEndDate();

        int totalClaims = claimService.getClaimsCreatedByUserForPeriod(user, startDate, endDate).size();
        int totalApprovedClaims = claimService.getClaimsCreatedByUserForPeriod(user, startDate, endDate).stream().filter(c -> c.getClaimStatus() == ClaimStatus.APPROVED)
                .toList().size();

        List<Transaction> transactions = transactionService.getTransactionsCreatedByUserForPeriod(user, startDate, endDate);

        BigDecimal totalReimbursedAmount = transactions.stream()
                .map(Transaction::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalTransactions = transactions.size();

        CreateSummaryRequest createSummaryRequest = CreateSummaryRequest.builder()
                .userId(user.getId())
                .startDate(startDate)
                .endDate(endDate)
                .totalClaims(totalClaims)
                .totalApprovedClaims(totalApprovedClaims)
                .totalReimbursedAmount(totalReimbursedAmount)
                .totalTransactions(totalTransactions)
                .build();

        try {
            client.createReport(createSummaryRequest);
        } catch (FeignException exception) {
            log.error("[S2S Call]: Failed due to %s.".formatted(exception.getMessage()));
            throw exception;
        }
    }
}
