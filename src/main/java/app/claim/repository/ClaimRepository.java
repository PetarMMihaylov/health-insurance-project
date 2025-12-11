package app.claim.repository;

import app.claim.model.Claim;
import app.claim.model.ClaimStatus;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    List<Claim> findAllByUserAndDeletedFalseOrderByUpdatedOnDesc(User user);

    List<Claim> findAllByClaimStatus(ClaimStatus claimStatus);

    List<Claim> findAllByUserAndDeletedFalseAndCreatedOnBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    List<Claim> findAllByOrderByUpdatedOnDesc();
}
