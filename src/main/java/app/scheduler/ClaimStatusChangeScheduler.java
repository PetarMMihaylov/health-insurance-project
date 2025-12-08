package app.scheduler;

import app.claim.service.ClaimService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClaimStatusChangeScheduler {

    private final ClaimService claimService;

    public ClaimStatusChangeScheduler(ClaimService claimService) {
        this.claimService = claimService;
    }

    @Scheduled(fixedDelayString = "PT5M")
    public void moveOpenClaimsToReview() {
        claimService.moveOpenClaimsToReview();
    }
}
