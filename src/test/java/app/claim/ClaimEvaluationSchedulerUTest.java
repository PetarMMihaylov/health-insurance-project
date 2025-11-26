package app.claim;

import app.claim.service.ClaimService;
import app.scheduler.ClaimEvaluationScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClaimEvaluationSchedulerUTest {

    @Mock
    private ClaimService claimService;

    @InjectMocks
    private ClaimEvaluationScheduler scheduler;

    @Test
    void autoEvaluateClaims_ShouldCallClaimService() {

        scheduler.autoEvaluateClaims();

        verify(claimService).evaluateClaims();
    }
}
