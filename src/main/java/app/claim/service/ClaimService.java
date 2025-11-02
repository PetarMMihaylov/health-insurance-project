package app.claim.service;
import app.claim.model.Claim;
import app.claim.model.ClaimStatus;
import app.claim.repository.ClaimRepository;
import app.exception.DomainException;
import app.transaction.model.Transaction;
import app.user.model.User;
import app.user.model.UserRole;
import app.web.dto.ClaimSubmissionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;

    @Autowired
    public ClaimService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
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
        return claimRepository.findById(id).orElseThrow(() -> new DomainException("No such claim has been found"));
    }


    public Claim getByIdForUser(UUID id, User user) {
        Claim claim = claimRepository.findById(id).orElseThrow(() -> new DomainException("Claim not found"));

        if (user.getRole() != UserRole.ADMIN) {
            if (!claim.getUser().equals(user) || claim.isDeleted()) {
                throw new DomainException("Access denied for this claim");
            }
        }

        return claim;
    }
}
