package app.report;

import app.claim.service.ClaimService;
import app.report.client.ReportClient;
import app.report.client.dto.Summary;
import app.transaction.service.TransactionService;
import app.report.service.ReportService;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceUTest {

    @Mock
    private ReportClient reportClient;

    @Mock
    private ClaimService claimService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ReportService reportService;

    @Test
    void getLastUserReports_ShouldReturnSummaries() {

        UUID userId = UUID.randomUUID();
        Summary summary1 = new Summary(UUID.randomUUID(), userId, null, null, 1, 1, null, 1, null);
        Summary summary2 = new Summary(UUID.randomUUID(), userId, null, null, 2, 2, null, 2, null);
        when(reportClient.getReports(userId)).thenReturn(ResponseEntity.ok(List.of(summary1, summary2)));

        List<Summary> result = reportService.getLastUserReports(userId);

        assertEquals(2, result.size());
        assertTrue(result.contains(summary1));
        assertTrue(result.contains(summary2));
        verify(reportClient).getReports(userId);
    }

    @Test
    void getLastUserReports_NullResponse_ShouldReturnEmptyList() {

        UUID userId = UUID.randomUUID();
        when(reportClient.getReports(userId)).thenReturn(ResponseEntity.ok(null));

        List<Summary> result = reportService.getLastUserReports(userId);

        assertTrue(result.isEmpty());
        verify(reportClient).getReports(userId);
    }

    @Test
    void getReportById_ValidUser_ShouldReturnSummary() {

        UUID userId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        Summary summary = new Summary(reportId, userId, null, null, 1, 1, null, 1, null);
        when(reportClient.getReportDetails(reportId)).thenReturn(ResponseEntity.ok(summary));

        Summary result = reportService.getReportById(reportId, userId);

        assertEquals(summary, result);
        verify(reportClient).getReportDetails(reportId);
    }

    @Test
    void getReportById_UserMismatch_ShouldThrowSecurityException() {

        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        Summary summary = new Summary(reportId, otherUserId, null, null, 1, 1, null, 1, null);
        when(reportClient.getReportDetails(reportId)).thenReturn(ResponseEntity.ok(summary));

        SecurityException exception = assertThrows(SecurityException.class,
                () -> reportService.getReportById(reportId, userId));

        assertEquals("Access denied: This report does not belong to you.", exception.getMessage());

        verify(reportClient).getReportDetails(reportId);
    }

    @Test
    void deleteReport_ValidUser_ShouldCallDelete() {

        UUID userId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        Summary summary = new Summary(reportId, userId, null, null, 1, 1, null, 1, null);
        when(reportClient.getReportDetails(reportId)).thenReturn(ResponseEntity.ok(summary));
        doNothing().when(reportClient).deleteReport(reportId);

        assertDoesNotThrow(() -> reportService.deleteReport(reportId, userId));

        verify(reportClient).getReportDetails(reportId);
        verify(reportClient).deleteReport(reportId);
    }

    @Test
    void deleteReport_UserMismatch_ShouldThrowSecurityException() {

        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        Summary summary = new Summary(reportId, otherUserId, null, null, 1, 1, null, 1, null);
        when(reportClient.getReportDetails(reportId)).thenReturn(ResponseEntity.ok(summary));

        SecurityException exception = assertThrows(SecurityException.class,
                () -> reportService.deleteReport(reportId, userId));
        assertEquals("Cannot delete report of another user.", exception.getMessage());

        verify(reportClient).getReportDetails(reportId);
        verify(reportClient, never()).deleteReport(reportId);
    }
}
