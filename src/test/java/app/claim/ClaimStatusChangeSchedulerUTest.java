package app.claim;

import app.claim.service.ClaimService;
import app.scheduler.ClaimStatusChangeScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClaimStatusChangeSchedulerUTest {

    @Mock
    private ClaimService claimService;

    @InjectMocks
    private ClaimStatusChangeScheduler scheduler;

    @Test
    void moveOpenClaimsToReview_ShouldCallClaimService() {

        scheduler.moveOpenClaimsToReview();

        verify(claimService).moveOpenClaimsToReview();
    }
}
