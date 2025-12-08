package app.scheduler;

import app.claim.service.ClaimService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClaimEvaluationScheduler {

    private final ClaimService claimService;

    public ClaimEvaluationScheduler(ClaimService claimService) {
        this.claimService = claimService;
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void autoEvaluateClaims() {
        claimService.evaluateClaims();
    }
}
