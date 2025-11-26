package app.claim;

import app.claim.model.*;
import app.claim.repository.ClaimRepository;
import app.claim.service.ClaimService;
import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.user.model.*;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ClaimServiceITest {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    private User createUserWithPolicy() {
        Policy policy = Policy.builder()
                .policyType(PolicyType.STANDARD)
                .limitForMedications(BigDecimal.valueOf(100))
                .limitForHospitalTreatment(BigDecimal.valueOf(1000))
                .limitForSurgery(BigDecimal.valueOf(1500))
                .limitForDentalService(BigDecimal.valueOf(500))
                .policyPrice(BigDecimal.TEN)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        User user = User.builder()
                .username("john")
                .email("john@test.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.POLICYHOLDER)
                .accountBalance(BigDecimal.valueOf(500))
                .policy(policy)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Test
    void moveOpenClaimsToReview_ShouldUpdateClaimStatus() {

        User user = createUserWithPolicy();

        Claim openClaim = Claim.builder()
                .user(user)
                .claimType(ClaimType.MEDICATION_EXPENSES)
                .claimStatus(ClaimStatus.OPEN)
                .requestedAmount(BigDecimal.valueOf(50))
                .attachedDocument("medication.pdf")
                .description("desc")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        claimRepository.save(openClaim);

        LocalDateTime before = LocalDateTime.now();

        claimService.moveOpenClaimsToReview();

        List<Claim> updatedClaims = claimRepository.findAll();
        assertEquals(1, updatedClaims.size());

        Claim claim = updatedClaims.get(0);
        assertEquals(ClaimStatus.FOR_REVIEW, claim.getClaimStatus());
        assertTrue(ChronoUnit.SECONDS.between(before, claim.getUpdatedOn()) < 5);
    }

    @Test
    void evaluateClaims_ShouldApproveOrRejectCorrectly() {

        User user = createUserWithPolicy();

        Claim claimToApprove = Claim.builder()
                .user(user)
                .claimType(ClaimType.MEDICATION_EXPENSES)
                .claimStatus(ClaimStatus.FOR_REVIEW)
                .requestedAmount(BigDecimal.valueOf(50))
                .attachedDocument("medication_receipt.pdf")
                .description("desc")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        Claim claimToReject = Claim.builder()
                .user(user)
                .claimType(ClaimType.MEDICATION_EXPENSES)
                .claimStatus(ClaimStatus.FOR_REVIEW)
                .requestedAmount(BigDecimal.valueOf(200))
                .attachedDocument("medication_receipt.pdf")
                .description("desc")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        claimRepository.saveAll(List.of(claimToApprove, claimToReject));

        LocalDateTime before = LocalDateTime.now();
        claimService.evaluateClaims();

        List<Claim> allClaims = claimRepository.findAll();

        Claim approved = allClaims.stream().filter(c -> c.getRequestedAmount().equals(BigDecimal.valueOf(50))).findFirst().orElseThrow();
        Claim rejected = allClaims.stream().filter(c -> c.getRequestedAmount().equals(BigDecimal.valueOf(200))).findFirst().orElseThrow();

        assertEquals(ClaimStatus.APPROVED, approved.getClaimStatus());
        assertEquals(ClaimStatus.REJECTED, rejected.getClaimStatus());

        assertEquals(BigDecimal.valueOf(550), userRepository.findById(user.getId()).get().getAccountBalance());

        assertTrue(ChronoUnit.SECONDS.between(before, approved.getUpdatedOn()) < 10);
        assertTrue(ChronoUnit.SECONDS.between(before, rejected.getUpdatedOn()) < 10);
    }
}
