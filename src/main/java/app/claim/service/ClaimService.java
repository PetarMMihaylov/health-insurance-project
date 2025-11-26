package app.claim.service;

import app.claim.model.Claim;
import app.claim.model.ClaimStatus;
import app.claim.model.ClaimType;
import app.claim.repository.ClaimRepository;
import app.exception.ClaimNotFoundException;
import app.exception.DomainException;
import app.policy.model.Policy;
import app.transaction.model.TransactionStatus;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.ClaimSubmissionRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final UserService userService;

    private final TransactionService transactionService;

    @Autowired
    public ClaimService(ClaimRepository claimRepository, UserService userService, TransactionService transactionService) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.transactionService = transactionService;
    }

    public Claim createClaim(ClaimSubmissionRequest claimSubmissionRequest, User user) {

        Claim claim = Claim.builder()
                .claimType(claimSubmissionRequest.getClaimType())
                .claimStatus(ClaimStatus.OPEN)
                .requestedAmount(claimSubmissionRequest.getRequestedAmount())
                .attachedDocument(claimSubmissionRequest.getAttachedDocument())
                .description(claimSubmissionRequest.getDescription())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .deleted(false)
                .user(user)
                .build();

        return claimRepository.save(claim);
    }

    public List<Claim> getClaimsCreatedByUserForPeriod(User user, LocalDate startDate, LocalDate endDate) {
        return claimRepository.findAllByUserAndDeletedFalseAndCreatedOnBetween(user, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
    }

    public List<Claim> getClaims(User user) {
        List <Claim> allClaims;
        if (user.getRole() == UserRole.ADMIN) {
            allClaims = claimRepository.findAll();
        } else {
            allClaims = claimRepository.findAllByUserAndDeletedFalse(user);
        }

        return allClaims;
    }

    public void softDelete(UUID id) {
        Claim claim = getById(id);
        claim.setDeleted(!claim.isDeleted());
        claim.setUpdatedOn(LocalDateTime.now());
        claimRepository.save(claim);
    }

    public Claim getById(UUID id) {
        return claimRepository.findById(id).orElseThrow(() -> new ClaimNotFoundException("No such claim has been found"));
    }


    public Claim getByIdForUser(UUID id, User user) {
        Claim claim = getById(id);

        if (user.getRole() != UserRole.ADMIN) {
            if (!claim.getUser().equals(user) || claim.isDeleted()) {
                throw new DomainException("Access denied for this claim");
            }
        }

        return claim;
    }

    @Transactional
    public void moveOpenClaimsToReview() {
        List<Claim> openClaims = getAllClaimsByStatus(ClaimStatus.OPEN);

        if (openClaims.isEmpty()) {
            log.info("Scheduler: No OPEN claims to process");
            return;
        }

        openClaims.forEach(claim -> {
            claim.setClaimStatus(ClaimStatus.FOR_REVIEW);
            claim.setUpdatedOn(LocalDateTime.now());
        });

        claimRepository.saveAll(openClaims);

        log.info("Scheduler: Moved {} claims from OPEN to FOR_REVIEW", openClaims.size());
    }

    @Transactional
    public void evaluateClaims() {
        List<Claim> claimsForEvaluation = getAllClaimsByStatus(ClaimStatus.FOR_REVIEW);

        claimsForEvaluation.forEach(claim -> {
            User user = claim.getUser();
            Policy policy = user.getPolicy();

            if (claim.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                rejectClaim(claim, LocalDateTime.now());
                user.setUpdatedOn(LocalDateTime.now());
                userService.persistUser(user);
                transactionService.create(user, BigDecimal.ZERO, TransactionStatus.FAILED);
                return;
            }

            String doc = claim.getAttachedDocument().toLowerCase();
            boolean invalidDoc =
                    (claim.getClaimType() == ClaimType.MEDICATION_EXPENSES && !doc.contains("medication")) ||
                            (claim.getClaimType() == ClaimType.HOSPITAL_TREATMENT_EXPENSES && !doc.contains("hospital_treatment")) ||
                            (claim.getClaimType() == ClaimType.SURGERY_EXPENSES && !doc.contains("surgery")) ||
                            (claim.getClaimType() == ClaimType.DENTAL_SERVICE_EXPENSES && !doc.contains("dental_service"));

            if (invalidDoc) {
                rejectClaim(claim, LocalDateTime.now());
                user.setUpdatedOn(LocalDateTime.now());
                userService.persistUser(user);
                transactionService.create(user, BigDecimal.ZERO, TransactionStatus.FAILED);
                return;
            }

            BigDecimal limit = switch (claim.getClaimType()) {
                case MEDICATION_EXPENSES -> policy.getLimitForMedications();
                case HOSPITAL_TREATMENT_EXPENSES -> policy.getLimitForHospitalTreatment();
                case SURGERY_EXPENSES -> policy.getLimitForSurgery();
                case DENTAL_SERVICE_EXPENSES -> policy.getLimitForDentalService();
            };

            boolean approved = claim.getRequestedAmount().compareTo(limit) <= 0;

            if (approved) {
                claim.setClaimStatus(ClaimStatus.APPROVED);
                user.setAccountBalance(user.getAccountBalance().add(claim.getRequestedAmount()));
                transactionService.create(user, claim.getRequestedAmount(), TransactionStatus.COMPLETED);
                log.info("Claim {} approved. User {} new balance = {}", claim.getId(), user.getUsername(), user.getAccountBalance());
            } else {
                claim.setClaimStatus(ClaimStatus.REJECTED);
                transactionService.create(user, BigDecimal.ZERO, TransactionStatus.FAILED);
            }

            claim.setUpdatedOn(LocalDateTime.now());
            user.setUpdatedOn(LocalDateTime.now());
            userService.persistUser(user);
        });

        claimRepository.saveAll(claimsForEvaluation);
        log.info("Scheduler: Automatically evaluated {} claims", claimsForEvaluation.size());
    }

    private void rejectClaim(Claim claim, LocalDateTime now) {
        claim.setClaimStatus(ClaimStatus.REJECTED);
        claim.setUpdatedOn(now);
    }

    public List<Claim> getAllClaimsByStatus(ClaimStatus claimStatus) {
        return claimRepository.findAllByClaimStatus(claimStatus);
    }
}
