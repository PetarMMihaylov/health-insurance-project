package app.claim;

import app.claim.model.*;
import app.claim.repository.ClaimRepository;
import app.claim.service.ClaimService;
import app.exception.ClaimNotFoundException;
import app.exception.DomainException;
import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.transaction.service.TransactionService;
import app.user.model.*;
import app.user.service.UserService;
import app.web.dto.ClaimSubmissionRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceUTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ClaimService claimService;

    private User buildUser(UserRole role) {
        return User.builder()
                .id(UUID.randomUUID())
                .email("email@test.com")
                .username("username")
                .password("pass")
                .firstName("John")
                .lastName("Doe")
                .role(role)
                .permission("not_delete")
                .companyName(CompanyName.LOCAL_GROUP_LTD)
                .employed(true)
                .accountBalance(BigDecimal.valueOf(3000))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .policy(Policy.builder()
                        .id(UUID.randomUUID())
                        .policyType(PolicyType.STANDARD)
                        .limitForMedications(BigDecimal.valueOf(100))
                        .limitForHospitalTreatment(BigDecimal.valueOf(1000))
                        .limitForSurgery(BigDecimal.valueOf(1500))
                        .limitForDentalService(BigDecimal.valueOf(500))
                        .policyPrice(BigDecimal.valueOf(200))
                        .createdOn(LocalDateTime.now())
                        .updatedOn(LocalDateTime.now())
                        .build())
                .build();
    }

    private Claim buildClaim(User user, boolean deleted) {

        return Claim.builder()
                .id(UUID.randomUUID())
                .claimType(ClaimType.MEDICATION_EXPENSES)
                .claimStatus(ClaimStatus.OPEN)
                .requestedAmount(BigDecimal.TEN)
                .attachedDocument("medication.pdf")
                .description("test desc")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .deleted(deleted)
                .user(user)
                .build();
    }

    @Test
    void createClaim_ShouldCreateAndSaveClaim_WithFieldAssertions() {

        User user = buildUser(UserRole.POLICYHOLDER);

        ClaimSubmissionRequest request = ClaimSubmissionRequest.builder()
                .claimType(ClaimType.MEDICATION_EXPENSES)
                .requestedAmount(BigDecimal.TEN)
                .attachedDocument("medication.pdf")
                .description("desc")
                .build();

        Claim saved = buildClaim(user, false);
        when(claimRepository.save(any(Claim.class))).thenReturn(saved);

        Claim result = claimService.createClaim(request, user);

        assertNotNull(result);
        assertEquals(user, result.getUser(), "User should be set correctly");
        assertEquals(request.getClaimType(), result.getClaimType(), "Claim type should match request");
        assertEquals(request.getRequestedAmount(), result.getRequestedAmount(), "Requested amount should match request");
        assertEquals(request.getAttachedDocument(), result.getAttachedDocument(), "Attached document should match request");
        assertEquals(request.getDescription(), result.getDescription(), "Description should match request");
        assertFalse(result.isDeleted(), "New claim should not be marked deleted");
        assertNotNull(result.getCreatedOn(), "CreatedOn should be set");
        assertNotNull(result.getUpdatedOn(), "UpdatedOn should be set");

        verify(claimRepository).save(any(Claim.class));
    }

    @Test
    void getClaimsCreatedByUserForPeriod_ShouldReturnClaims() {

        User user = buildUser(UserRole.POLICYHOLDER);

        List<Claim> claims = List.of(buildClaim(user, false));

        when(claimRepository.findAllByUserAndDeletedFalseAndCreatedOnBetween(
                eq(user), any(), any()
        )).thenReturn(claims);

        List<Claim> result = claimService.getClaimsCreatedByUserForPeriod(
                user,
                LocalDate.now().minusDays(5),
                LocalDate.now()
        );

        assertEquals(1, result.size());
    }

    @Test
    void getClaimsCreatedByUserForPeriod_NoClaims_ShouldReturnEmptyList() {

        User user = buildUser(UserRole.POLICYHOLDER);

        when(claimRepository.findAllByUserAndDeletedFalseAndCreatedOnBetween(eq(user), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        List<Claim> result = claimService.getClaimsCreatedByUserForPeriod(user, LocalDate.now().minusDays(10), LocalDate.now());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getClaimsCreatedByUserForPeriod_StartAfterEnd_ShouldReturnEmptyList() {

        User user = buildUser(UserRole.POLICYHOLDER);
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().minusDays(5);

        when(claimRepository.findAllByUserAndDeletedFalseAndCreatedOnBetween(eq(user), eq(start.atStartOfDay()), eq(end.atTime(LocalTime.MAX)))).thenReturn(Collections.emptyList());

        List<Claim> result = claimService.getClaimsCreatedByUserForPeriod(user, start, end);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getClaims_Admin_ShouldReturnAllClaims() {

        User admin = buildUser(UserRole.ADMIN);
        User policyholder = buildUser(UserRole.POLICYHOLDER);

        List<Claim> claims = List.of(
                buildClaim(admin, false),
                buildClaim(policyholder, false)
        );

        when(claimRepository.findAll()).thenReturn(claims);

        List<Claim> result = claimService.getClaims(admin);

        assertEquals(2, result.size());
        verify(claimRepository).findAll();
    }

    @Test
    void getClaims_User_ShouldReturnTheirClaims() {

        User user = buildUser(UserRole.POLICYHOLDER);

        List<Claim> claims = List.of(buildClaim(user, false));

        when(claimRepository.findAllByUserAndDeletedFalse(user)).thenReturn(claims);

        List<Claim> result = claimService.getClaims(user);

        assertEquals(1, result.size());
        verify(claimRepository).findAllByUserAndDeletedFalse(user);
    }

    @Test
    void getClaims_UserWithNoClaims_ShouldReturnEmptyList() {

        User user = buildUser(UserRole.POLICYHOLDER);

        when(claimRepository.findAllByUserAndDeletedFalse(user)).thenReturn(Collections.emptyList());

        List<Claim> result = claimService.getClaims(user);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(claimRepository).findAllByUserAndDeletedFalse(user);
    }

    @Test
    void softDelete_ShouldToggleDeleted() {

        User user = buildUser(UserRole.POLICYHOLDER);
        Claim claim = buildClaim(user, false);

        UUID id = claim.getId();

        when(claimRepository.findById(id)).thenReturn(Optional.of(claim));

        claimService.softDelete(id);

        assertTrue(claim.isDeleted());
        verify(claimRepository).save(claim);
    }

    @Test
    void getById_ShouldReturnClaim() {

        Claim claim = buildClaim(buildUser(UserRole.POLICYHOLDER), false);

        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));

        Claim result = claimService.getById(claim.getId());

        assertEquals(claim, result);
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {

        when(claimRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ClaimNotFoundException.class,
                () -> claimService.getById(UUID.randomUUID()));
    }

    @Test
    void getByIdForUser_Admin_ShouldReturnClaim() {

        User admin = buildUser(UserRole.ADMIN);
        Claim claim = buildClaim(admin, false);

        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));

        Claim result = claimService.getByIdForUser(claim.getId(), admin);

        assertEquals(claim, result);
    }

    @Test
    void getByIdForUser_Owner_ShouldReturnClaim() {

        User user = buildUser(UserRole.POLICYHOLDER);
        Claim claim = buildClaim(user, false);

        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));

        Claim result = claimService.getByIdForUser(claim.getId(), user);

        assertEquals(claim, result);
    }

    @Test
    void getByIdForUser_ShouldThrow_WhenUserNotOwner() {

        User owner = buildUser(UserRole.POLICYHOLDER);
        User other = buildUser(UserRole.POLICYHOLDER);

        Claim claim = buildClaim(owner, false);

        when(claimRepository.findById(any())).thenReturn(Optional.of(claim));

        assertThrows(DomainException.class,
                () -> claimService.getByIdForUser(UUID.randomUUID(), other));
    }

    @Test
    void getByIdForUser_ShouldThrow_WhenClaimDeleted() {

        User user = buildUser(UserRole.POLICYHOLDER);

        Claim claim = buildClaim(user, true);

        when(claimRepository.findById(any())).thenReturn(Optional.of(claim));

        assertThrows(DomainException.class,
                () -> claimService.getByIdForUser(UUID.randomUUID(), user));
    }

    @Test
    void getAllClaimsByStatus_ShouldReturnClaims() {

        User user = buildUser(UserRole.POLICYHOLDER);
        List<Claim> claims = List.of(buildClaim(user, false));

        when(claimRepository.findAllByClaimStatus(ClaimStatus.OPEN)).thenReturn(claims);

        List<Claim> result = claimService.getAllClaimsByStatus(ClaimStatus.OPEN);

        assertEquals(1, result.size());
    }

    @Test
    void getAllClaimsByStatus_NoClaims_ShouldReturnEmptyList() {

        when(claimRepository.findAllByClaimStatus(ClaimStatus.OPEN)).thenReturn(Collections.emptyList());

        List<Claim> result = claimService.getAllClaimsByStatus(ClaimStatus.OPEN);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(claimRepository).findAllByClaimStatus(ClaimStatus.OPEN);
    }
}
