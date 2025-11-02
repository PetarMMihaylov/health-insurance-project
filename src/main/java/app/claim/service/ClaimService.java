package app.claim.service;
import app.claim.model.Claim;
import app.claim.model.ClaimStatus;
import app.claim.repository.ClaimRepository;
import app.user.model.User;
import app.web.dto.ClaimSubmissionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
                .user(user)
                .build();

        return claimRepository.save(claim);
    }
}
