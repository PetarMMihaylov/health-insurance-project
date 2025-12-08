package app.policy;

import app.exception.PolicyNotFoundException;
import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.policy.service.PolicyService;
import app.security.AuthenticationMetadata;
import app.user.model.CompanyName;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.PolicyController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyController.class)
class PolicyControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PolicyService policyService;

    @MockitoBean
    private UserService userService;

    private Policy createDummyPolicy() {
        return Policy.builder()
                .id(UUID.randomUUID())
                .policyType(PolicyType.STANDARD)
                .limitForMedications(BigDecimal.valueOf(100))
                .limitForHospitalTreatment(BigDecimal.valueOf(200))
                .limitForSurgery(BigDecimal.valueOf(300))
                .limitForDentalService(BigDecimal.valueOf(50))
                .policyPrice(BigDecimal.valueOf(500))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    private User createDummyUser(UserRole role, Policy policy) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(role.name().toLowerCase() + "@example.com")
                .username(role.name().toLowerCase() + "_user")
                .password("password")
                .firstName(role.name())
                .lastName("User")
                .role(role)
                .permission("ALL")
                .companyName(CompanyName.LOCAL_GROUP_LTD)
                .accountBalance(BigDecimal.valueOf(1000))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .policy(policy)
                .employed(true)
                .build();
    }

    private AuthenticationMetadata authenticationMetadata(User user) {
        return new AuthenticationMetadata(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                user.getPermission(),
                user.isEmployed()
        );
    }

    @Test
    void getAllPoliciesPage_ShouldReturnPolicyViewWithModel() throws Exception {

        Policy policy = createDummyPolicy();
        User user = createDummyUser(UserRole.POLICYHOLDER, policy);
        AuthenticationMetadata auth = authenticationMetadata(user);

        when(userService.getById(user.getId())).thenReturn(user);
        when(policyService.getPolicies()).thenReturn(List.of(policy));

        mockMvc.perform(get("/policy")
                        .with(user(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("policy"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("allPolicies"));
    }

    @Test
    void getPolicyChangeLimitsForm_nonAdmin_fails() throws Exception {
        Policy policy = createDummyPolicy();
        User user = createDummyUser(UserRole.POLICYHOLDER, policy);
        AuthenticationMetadata auth = authenticationMetadata(user);

        mockMvc.perform(get("/policy/{id}/policy-settings", policy.getId())
                        .with(user(auth)))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(policyService);
    }

    @Test
    void getPolicyChangeLimitsForm_admin_success() throws Exception {
        Policy policy = createDummyPolicy();
        User admin = createDummyUser(UserRole.ADMIN, policy);
        AuthenticationMetadata auth = authenticationMetadata(admin);

        when(policyService.getById(policy.getId())).thenReturn(policy);

        mockMvc.perform(get("/policy/{id}/policy-settings", policy.getId())
                        .with(user(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("policy-limits-form"))
                .andExpect(model().attributeExists("policy"))
                .andExpect(model().attributeExists("policyLimitsChangeRequest"));

        verify(policyService).getById(policy.getId());
    }

    @Test
    void changePolicyLimits_admin_successfulUpdate_redirectsToPolicyPage() throws Exception {
        Policy policy = createDummyPolicy();
        User admin = createDummyUser(UserRole.ADMIN, policy);
        AuthenticationMetadata auth = authenticationMetadata(admin);

        when(policyService.getById(policy.getId())).thenReturn(policy);
        when(userService.getById(admin.getId())).thenReturn(admin);

        mockMvc.perform(post("/policy/{id}/policy-settings", policy.getId())
                        .with(user(auth))
                        .with(csrf())
                        .param("limitForMedications", "100")
                        .param("limitForHospitalTreatment", "200")
                        .param("limitForSurgery", "300")
                        .param("limitForDentalService", "50")
                        .param("policyPrice", "500")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/policy"));

        verify(policyService).getById(policy.getId());
        verify(policyService).updatePolicyLimits(eq(policy), any(), eq(admin));
    }

    @Test
    void changePolicyLimits_policyNotFound_failsAndRedirects() throws Exception {
        Policy policy = createDummyPolicy();
        User admin = createDummyUser(UserRole.ADMIN, policy);
        AuthenticationMetadata auth = authenticationMetadata(admin);

        when(policyService.getById(policy.getId())).thenThrow(new PolicyNotFoundException("Policy not found"));

        mockMvc.perform(post("/policy/{id}/policy-settings", policy.getId())
                        .with(user(auth))
                        .with(csrf())
                        .param("limitForMedications", "100")
                        .param("limitForHospitalTreatment", "200")
                        .param("limitForSurgery", "300")
                        .param("limitForDentalService", "50")
                        .param("policyPrice", "500")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/policy"));

        verify(policyService).getById(policy.getId());
    }

    @Test
    void changePolicyLimits_nonAdmin_forbidden() throws Exception {
        Policy policy = createDummyPolicy();
        User user = createDummyUser(UserRole.POLICYHOLDER, policy);
        AuthenticationMetadata auth = authenticationMetadata(user);

        mockMvc.perform(post("/policy/{id}/policy-settings", policy.getId())
                        .with(user(auth))
                        .with(csrf())
                        .param("limitForMedications", "100")
                        .param("limitForHospitalTreatment", "200")
                        .param("limitForSurgery", "300")
                        .param("limitForDentalService", "50")
                        .param("policyPrice", "500")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(policyService);
    }

    @Test
    void changePolicyLimits_withInvalidNegativeNumbers_rendersFormAgain() throws Exception {
        Policy policy = createDummyPolicy();
        User admin = createDummyUser(UserRole.ADMIN, policy);
        AuthenticationMetadata auth = authenticationMetadata(admin);

        when(policyService.getById(policy.getId())).thenReturn(policy);

        mockMvc.perform(post("/policy/{id}/policy-settings", policy.getId())
                        .with(user(auth))
                        .with(csrf())
                        .param("limitForMedications", "-100")
                        .param("limitForHospitalTreatment", "-200")
                        .param("limitForSurgery", "-300")
                        .param("limitForDentalService", "-50")
                        .param("policyPrice", "-500")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("policy-limits-form"))
                .andExpect(model().attributeExists("policy"))
                .andExpect(model().attributeExists("policyLimitsChangeRequest"));

        verify(policyService).getById(policy.getId());
    }

    @Test
    void changeUserPolicy_redirectsAndCallsService() throws Exception {
        Policy policy = createDummyPolicy();
        User user = createDummyUser(UserRole.POLICYHOLDER, policy);
        AuthenticationMetadata auth = authenticationMetadata(user);

        when(userService.getById(user.getId())).thenReturn(user);

        mockMvc.perform(patch("/policy/{id}/user-policy", policy.getId())
                        .with(user(auth))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/policy?error=balance"));

        verify(userService).changePolicy(policy.getId(), user);
    }

    @Test
    void changeUserPolicy_successfulChange_redirectsToPolicy() throws Exception {
        Policy policy = createDummyPolicy();
        User user = createDummyUser(UserRole.POLICYHOLDER, policy);
        AuthenticationMetadata auth = authenticationMetadata(user);

        when(userService.getById(user.getId())).thenReturn(user);
        when(userService.changePolicy(policy.getId(), user)).thenReturn(true);

        mockMvc.perform(patch("/policy/{id}/user-policy", policy.getId())
                        .with(user(auth))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/policy"));

        verify(userService).changePolicy(policy.getId(), user);
    }
}
