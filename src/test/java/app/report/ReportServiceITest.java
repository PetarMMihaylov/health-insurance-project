package app.report;

import app.claim.model.Claim;
import app.claim.model.ClaimStatus;
import app.claim.model.ClaimType;
import app.claim.repository.ClaimRepository;
import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.repository.TransactionRepository;
import app.user.model.CompanyName;
import app.user.model.User;
import app.user.model.UserRole;
import app.report.service.ReportService;
import app.user.service.UserService;
import app.web.dto.CreateSummaryByDates;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportServiceITest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    @Test
    void createReport_ShouldWorkWithValidEntities() {

        Policy policy = Policy.builder()
                .policyType(PolicyType.STANDARD)
                .limitForMedications(BigDecimal.valueOf(1000))
                .limitForHospitalTreatment(BigDecimal.valueOf(5000))
                .limitForSurgery(BigDecimal.valueOf(2000))
                .limitForDentalService(BigDecimal.valueOf(1500))
                .policyPrice(BigDecimal.valueOf(100))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        User user = User.builder()
                .email("john@example.com")
                .username("john")
                .password("Password@1")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.POLICYHOLDER)
                .permission("not_delete")
                .companyName(CompanyName.NEURO_NEST)
                .employed(true)
                .accountBalance(BigDecimal.valueOf(1000))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .policy(policy)
                .build();

        userService.persistUser(user);

        Claim claim1 = Claim.builder()
                .user(user)
                .claimType(ClaimType.MEDICATION_EXPENSES)
                .claimStatus(ClaimStatus.APPROVED)
                .requestedAmount(BigDecimal.valueOf(100))
                .attachedDocument("medication.pdf")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .deleted(false)
                .build();

        Claim claim2 = Claim.builder()
                .user(user)
                .claimType(ClaimType.MEDICATION_EXPENSES)
                .claimStatus(ClaimStatus.REJECTED)
                .requestedAmount(BigDecimal.valueOf(50))
                .attachedDocument("doc123.pdf")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .deleted(false)
                .build();

        claimRepository.save(claim1);
        claimRepository.save(claim2);

        Transaction t1 = Transaction.builder()
                .transactionOwner(user)
                .transactionStatus(TransactionStatus.COMPLETED)
                .referenceNumber("ABCDEFGH")
                .paidAmount(BigDecimal.valueOf(100))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .deleted(false)
                .build();

        Transaction t2 = Transaction.builder()
                .transactionOwner(user)
                .transactionStatus(TransactionStatus.COMPLETED)
                .referenceNumber("IJKLMNOP")
                .paidAmount(BigDecimal.valueOf(50))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .deleted(false)
                .build();

        transactionRepository.save(t1);
        transactionRepository.save(t2);

        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        CreateSummaryByDates dto = new CreateSummaryByDates(startDate, endDate);

        assertDoesNotThrow(() -> reportService.createReport(dto, user));
    }
}
