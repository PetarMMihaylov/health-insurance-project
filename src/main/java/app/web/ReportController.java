package app.web;

import app.report.client.dto.CreateSummaryRequest;
import app.report.client.dto.Summary;
import app.report.service.ReportService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CreateSummaryByDates;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    @Autowired
    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getReportsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        ModelAndView modelAndView = new ModelAndView("reports");

        List<Summary> summaries = reportService.getLastUserReports(authenticationMetadata.getUserId());
        modelAndView.addObject("summaries", summaries);

        return modelAndView;
    }

    @GetMapping("/{id}")
    public ModelAndView getReportDetailsById(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        ModelAndView modelAndView = new ModelAndView("report-details");

        Summary summary = reportService.getReportById(id, authenticationMetadata.getUserId());
        modelAndView.addObject("summary", summary);

        return modelAndView;
    }

    @GetMapping("/new-report")
    public ModelAndView getNewSummaryPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("create-report");
        modelAndView.addObject("createSummaryByDates", new CreateSummaryByDates());

        return modelAndView;
    }

    @PostMapping("/new-report")
    public ModelAndView submitSummary(@Valid CreateSummaryByDates createSummaryByDates, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("create-report");
            return modelAndView;
        }

        reportService.createReport(createSummaryByDates, user);

        return new ModelAndView("redirect:/reports");
    }

    @DeleteMapping("/{id}")
    public String deleteReport(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        reportService.deleteReport(id, authenticationMetadata.getUserId());

        return "redirect:/reports";
    }
}
