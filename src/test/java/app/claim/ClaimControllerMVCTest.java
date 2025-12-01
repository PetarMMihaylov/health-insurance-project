package app.claim;

import app.claim.model.*;
import app.claim.service.ClaimService;
import app.exception.ClaimNotFoundException;
import app.exception.DomainException;
import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.security.AuthenticationMetadata;
import app.user.model.*;
import app.user.service.UserService;
import app.web.ClaimController;
import app.web.dto.ClaimSubmissionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClaimController.class)
@AutoConfigureMockMvc
class ClaimControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClaimService claimService;

    @MockitoBean
    private UserService userService;

    private User createDummyUser(UserRole role) {

        return User.builder()
                .id(UUID.randomUUID())
                .email("email@test.com")
                .username("username")
                .password("pass")
                .firstName("John")
                .lastName("Doe")
                .role(role)
                .permission(role == UserRole.ADMIN ? "can_delete" : "not_delete")
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

    private Claim createDummyClaim(User user, ClaimStatus status) {

        return Claim.builder()
                .id(UUID.randomUUID())
                .claimType(ClaimType.MEDICATION_EXPENSES)
                .claimStatus(status)
                .requestedAmount(BigDecimal.TEN)
                .attachedDocument("medication.pdf")
                .description("Test claim")
                .user(user)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    private AuthenticationMetadata auth(User user) {

        return new AuthenticationMetadata(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), user.getPermission(), user.isEmployed());
    }

    @Test
    void getAllClaimsPage_returnsClaimsView() throws Exception {

        User user = createDummyUser(UserRole.POLICYHOLDER);
        Claim claim = createDummyClaim(user, ClaimStatus.OPEN);

        when(userService.getById(user.getId())).thenReturn(user);
        when(claimService.getClaims(user)).thenReturn(List.of(claim));

        mockMvc.perform(get("/claims").with(user(auth(user))))
                .andExpect(status().isOk())
                .andExpect(view().name("claims"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("allClaims"));

        verify(userService).getById(user.getId());
        verify(claimService).getClaims(user);
    }

    @Test
    void getNewClaimPage_returnsCreateClaimView() throws Exception {

        User user = createDummyUser(UserRole.POLICYHOLDER);

        when(userService.getById(user.getId())).thenReturn(user);

        mockMvc.perform(get("/claims/new-claim").with(user(auth(user))))
                .andExpect(status().isOk())
                .andExpect(view().name("create-claim"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("claimSubmissionRequest"));

        verify(userService).getById(user.getId());
    }

    @Test
    void getClaimDetails_returnsClaimDetailsView() throws Exception {

        User user = createDummyUser(UserRole.POLICYHOLDER);
        Claim claim = createDummyClaim(user, ClaimStatus.OPEN);

        when(userService.getById(user.getId())).thenReturn(user);
        when(claimService.getByIdForUser(claim.getId(), user)).thenReturn(claim);

        mockMvc.perform(get("/claims/{id}", claim.getId()).with(user(auth(user))))
                .andExpect(status().isOk())
                .andExpect(view().name("claim-details"))
                .andExpect(model().attributeExists("claim"));

        verify(userService).getById(user.getId());
        verify(claimService).getByIdForUser(claim.getId(), user);
    }

    @Test
    void getClaimDetails_claimBelongsToAnotherUser_throwsDomainException() throws Exception {

        User user = createDummyUser(UserRole.POLICYHOLDER);
        User other = createDummyUser(UserRole.POLICYHOLDER);
        Claim claim = createDummyClaim(other, ClaimStatus.OPEN);

        when(userService.getById(user.getId())).thenReturn(user);
        when(claimService.getByIdForUser(claim.getId(), user)).thenThrow(new DomainException("Access denied for this claim"));
        mockMvc.perform(get("/claims/{id}", claim.getId()).with(user(auth(user)))).andExpect(status().isInternalServerError());

        verify(userService).getById(user.getId());
        verify(claimService).getByIdForUser(claim.getId(), user);
    }

    @Test
    void getClaimDetails_claimNotFound_redirectsToClaims() throws Exception {
        User user = createDummyUser(UserRole.POLICYHOLDER);
        UUID claimId = UUID.randomUUID();

        when(userService.getById(user.getId())).thenReturn(user);
        when(claimService.getByIdForUser(claimId, user))
                .thenThrow(new ClaimNotFoundException("No such claim has been found"));

        mockMvc.perform(get("/claims/{id}", claimId).with(user(auth(user))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/claims"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(userService).getById(user.getId());
        verify(claimService).getByIdForUser(claimId, user);
    }


    @Test
    void submitClaim_validSubmission_redirects() throws Exception {

        User user = createDummyUser(UserRole.POLICYHOLDER);

        when(userService.getById(user.getId())).thenReturn(user);

        mockMvc.perform(post("/claims/new-claim")
                        .with(user(auth(user)))
                        .with(csrf())
                        .param("claimType", "MEDICATION_EXPENSES")
                        .param("requestedAmount", "10")
                        .param("attachedDocument", "medication.pdf")
                        .param("description", "desc")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/claims"));

        verify(claimService).createClaim(any(ClaimSubmissionRequest.class), eq(user));
    }

    @Test
    void submitClaim_withValidationErrors_rendersFormAgain() throws Exception {

        User user = createDummyUser(UserRole.POLICYHOLDER);

        when(userService.getById(user.getId())).thenReturn(user);

        mockMvc.perform(post("/claims/new-claim")
                        .with(user(auth(user)))
                        .with(csrf())
                        .param("description", "")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("create-claim"))
                .andExpect(model().attributeExists("user"));

        verifyNoInteractions(claimService);
    }

    @Test
    void deleteClaim_adminWithPermission_redirects() throws Exception {

        User admin = createDummyUser(UserRole.ADMIN);

        mockMvc.perform(delete("/claims/{id}/deletion", UUID.randomUUID())
                        .with(user(auth(admin)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/claims"));

        verify(claimService).softDelete(any(UUID.class));
    }

    @Test
    void deleteClaim_nonAdmin_error() throws Exception {

        User user = createDummyUser(UserRole.POLICYHOLDER);

        mockMvc.perform(delete("/claims/{id}/deletion", UUID.randomUUID()).with(user(auth(user))).with(csrf())).andExpect(status().isInternalServerError());

        verifyNoInteractions(claimService);
    }
}
