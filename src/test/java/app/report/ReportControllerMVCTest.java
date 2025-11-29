package app.report;

import app.report.client.dto.Summary;
import app.report.service.ReportService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.ReportController;
import app.web.dto.CreateSummaryByDates;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private UserService userService;

    @Test
    void getReportsPage_ShouldReturnReports() throws Exception {

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata auth = new AuthenticationMetadata(userId, "john", "Password@1", UserRole.POLICYHOLDER, "not_delete", true);

        Summary summary = new Summary(UUID.randomUUID(), userId, LocalDate.now().minusDays(5), LocalDate.now(), 5, 3, BigDecimal.valueOf(500), 10, LocalDateTime.now());
        when(reportService.getLastUserReports(userId)).thenReturn(List.of(summary));

        mockMvc.perform(get("/reports").with(user(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("reports"))
                .andExpect(model().attributeExists("summaries"))
                .andExpect(model().attribute("summaries", List.of(summary)));

        verify(reportService).getLastUserReports(userId);
    }

    @Test
    void getReportsPage_NoAuth_ShouldRedirect() throws Exception {

        mockMvc.perform(get("/reports"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void getReportDetailsById_ShouldReturnReportDetails() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        AuthenticationMetadata auth = new AuthenticationMetadata(userId, "john", "Password@1", UserRole.POLICYHOLDER, "not_delete", true);

        Summary summary = new Summary(reportId, userId, LocalDate.now().minusDays(5), LocalDate.now(), 5, 3, BigDecimal.valueOf(500), 10, LocalDateTime.now());
        when(reportService.getReportById(reportId, userId)).thenReturn(summary);

        mockMvc.perform(get("/reports/" + reportId).with(user(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("report-details"))
                .andExpect(model().attributeExists("summary"))
                .andExpect(model().attribute("summary", summary));

        verify(reportService).getReportById(reportId, userId);
    }

    @Test
    void getReportDetailsById_NoAuth_ShouldRedirect() throws Exception {

        mockMvc.perform(get("/reports/" + UUID.randomUUID()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void getNewSummaryPage_ShouldReturnCreateReportView() throws Exception {

        mockMvc.perform(get("/reports/new-report"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-report"))
                .andExpect(model().attributeExists("createSummaryByDates"));
    }

    @Test
    void submitSummary_Valid_ShouldRedirect() throws Exception {

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata auth = new AuthenticationMetadata(userId, "john", "Password@1", UserRole.POLICYHOLDER, "not_delete", true);
        User user = new User();
        user.setId(userId);
        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(post("/reports/new-report")
                        .with(csrf())
                        .with(user(auth))
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports"));

        verify(reportService).createReport(any(CreateSummaryByDates.class), eq(user));
    }

    @Test
    void submitSummary_Invalid_ShouldReturnCreateReportView() throws Exception {

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata auth = new AuthenticationMetadata(userId, "john", "Password@1", UserRole.POLICYHOLDER, "not_delete", true);

        mockMvc.perform(post("/reports/new-report")
                        .with(csrf())
                        .with(user(auth))
                        .param("startDate", "")
                        .param("endDate", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("create-report"));

        verify(reportService, never()).createReport(any(), any());
    }

    @Test
    void deleteReport_ShouldRedirectAndCallService() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        AuthenticationMetadata auth = new AuthenticationMetadata(userId, "john", "Password@1", UserRole.POLICYHOLDER, "not_delete", true);

        doNothing().when(reportService).deleteReport(reportId, userId);

        mockMvc.perform(delete("/reports/" + reportId)
                        .with(user(auth))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports"));

        verify(reportService).deleteReport(reportId, userId);
    }

    @Test
    void deleteReport_NoAuth_ShouldRedirect() throws Exception {

        mockMvc.perform(delete("/reports/" + UUID.randomUUID()))
                .andExpect(status().is3xxRedirection());
    }
}
