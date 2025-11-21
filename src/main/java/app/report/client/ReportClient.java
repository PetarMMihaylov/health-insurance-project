package app.report.client;

import app.report.client.dto.CreateSummaryRequest;
import app.report.client.dto.Summary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "reports-svc", url = "http://localhost:8081/api/v1")
public interface ReportClient {

    @GetMapping("/reports")
    ResponseEntity<List<Summary>> getReports(@RequestParam("userId") UUID userId);

    @GetMapping("/reports/{id}")
    ResponseEntity<Summary> getReportDetails(@PathVariable UUID id);

    @PostMapping("/reports")
    public ResponseEntity<Void> createReport(@RequestBody CreateSummaryRequest request);

    @DeleteMapping("/reports/{id}")
    ResponseEntity<Void> deleteReport(@PathVariable UUID id);
}
