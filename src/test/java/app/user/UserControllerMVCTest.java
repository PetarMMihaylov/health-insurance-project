package app.user;

import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.security.AuthenticationMetadata;
import app.user.model.CompanyName;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.UserController;
import app.web.dto.AccountBalanceRequest;
import app.web.dto.ProfileEditRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private User createTestUser(UUID id, UserRole role) {
        return User.builder()
                .id(id)
                .username("user123")
                .email("user123@example.com")
                .password("Password@1")
                .firstName("John")
                .lastName("Doe")
                .role(role)
                .permission(role == UserRole.ADMIN ? "can_delete" : "not_delete")
                .companyName(CompanyName.NEURO_NEST)
                .employed(true)
                .accountBalance(BigDecimal.valueOf(1000))
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

    @Test
    void getUsersAsAdmin_returnsUsers() throws Exception {

        UUID adminId = UUID.randomUUID();
        User admin = createTestUser(adminId, UserRole.ADMIN);
        when(userService.getAllUsers()).thenReturn(List.of(admin));

        mockMvc.perform(get("/users")
                        .with(user(new AuthenticationMetadata(adminId, admin.getUsername(), admin.getPassword(), admin.getRole(), admin.getPermission(), admin.isEmployed()))))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUsersAsNonAdmin_fails() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, UserRole.POLICYHOLDER);
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/users")
                        .with(user(new AuthenticationMetadata(userId, user.getUsername(), user.getPassword(), user.getRole(), user.getPermission(), user.isEmployed()))))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).getAllUsers();
    }

    @Test
    void getProfilePage_returnsProfilePage() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, UserRole.POLICYHOLDER);
        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/profile")
                        .with(user(new AuthenticationMetadata(userId, user.getUsername(), user.getPassword(), user.getRole(), user.getPermission(), user.isEmployed()))))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-update"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("profileEditRequest"));

        verify(userService, times(1)).getById(userId);
    }

    @Test
    void updateProfile_valid() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, UserRole.POLICYHOLDER);

        mockMvc.perform(put("/users/profile")
                        .with(csrf())
                        .with(user(new AuthenticationMetadata(userId, user.getUsername(), user.getPassword(), user.getRole(), user.getPermission(), user.isEmployed())))
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("password", "Password1@"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService, times(1)).updateProfile(eq(userId), any(ProfileEditRequest.class));
    }

    @Test
    void updateProfile_invalid() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, UserRole.POLICYHOLDER);
        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(put("/users/profile")
                        .with(csrf())
                        .with(user(new AuthenticationMetadata(userId, user.getUsername(), user.getPassword(), user.getRole(), user.getPermission(), user.isEmployed())))
                        .param("firstName", "")
                        .param("lastName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-update"))
                .andExpect(model().attributeExists("user"));

        verify(userService, never()).updateProfile(any(), any());
    }

    @Test
    void getBalancePage() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, UserRole.POLICYHOLDER);
        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/balance")
                        .with(user(new AuthenticationMetadata(userId, user.getUsername(), user.getPassword(), user.getRole(), user.getPermission(), user.isEmployed()))))
                .andExpect(status().isOk())
                .andExpect(view().name("change-balance"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("accountBalanceRequest"));

        verify(userService, times(1)).getById(userId);
    }

    @Test
    void updateBalance_valid() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, UserRole.POLICYHOLDER);

        mockMvc.perform(put("/users/balance")
                        .with(csrf())
                        .with(user(new AuthenticationMetadata(userId, user.getUsername(), user.getPassword(), user.getRole(), user.getPermission(), user.isEmployed())))
                        .param("addedAmount", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService, times(1)).updateBalance(eq(userId), any(AccountBalanceRequest.class));
    }

    @Test
    void updateBalance_invalid() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, UserRole.POLICYHOLDER);
        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(put("/users/balance")
                        .with(csrf())
                        .with(user(new AuthenticationMetadata(userId, user.getUsername(), user.getPassword(), user.getRole(), user.getPermission(), user.isEmployed())))
                        .param("addedAmount", "-100"))
                .andExpect(status().isOk())
                .andExpect(view().name("change-balance"))
                .andExpect(model().attributeExists("user"));

        verify(userService, never()).updateBalance(any(), any());
    }

    @Test
    void changeUserRole_asAdmin_returnsRedirect() throws Exception {

        UUID userId = UUID.randomUUID();
        User admin = createTestUser(UUID.randomUUID(), UserRole.ADMIN);

        mockMvc.perform(patch("/users/" + userId + "/role")
                        .with(csrf())
                        .with(user(new AuthenticationMetadata(admin.getId(), admin.getUsername(), admin.getPassword(), admin.getRole(), admin.getPermission(), admin.isEmployed()))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService, times(1)).changeRole(userId);
    }

    @Test
    void changeUserRole_asNonAdmin_fails() throws Exception {
        UUID userId = UUID.randomUUID();
        User nonAdmin = createTestUser(UUID.randomUUID(), UserRole.POLICYHOLDER);

        mockMvc.perform(patch("/users/" + userId + "/role")
                        .with(csrf())
                        .with(user(new AuthenticationMetadata(nonAdmin.getId(), nonAdmin.getUsername(), nonAdmin.getPassword(), nonAdmin.getRole(), nonAdmin.getPermission(), nonAdmin.isEmployed()))))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).changeRole(any());
    }

    @Test
    void changeUserEmployment_asAdmin_returnsRedirect() throws Exception {

        UUID userId = UUID.randomUUID();
        User admin = createTestUser(UUID.randomUUID(), UserRole.ADMIN);

        mockMvc.perform(patch("/users/" + userId + "/employment")
                        .with(csrf())
                        .with(user(new AuthenticationMetadata(admin.getId(), admin.getUsername(), admin.getPassword(), admin.getRole(), admin.getPermission(), admin.isEmployed()))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService, times(1)).changeEmployment(userId);
    }

    @Test
    void changeUserEmployment_asNonAdmin_fails() throws Exception {
        UUID userId = UUID.randomUUID();
        User nonAdmin = createTestUser(UUID.randomUUID(), UserRole.POLICYHOLDER);

        mockMvc.perform(patch("/users/" + userId + "/employment")
                        .with(csrf())
                        .with(user(new AuthenticationMetadata(nonAdmin.getId(), nonAdmin.getUsername(), nonAdmin.getPassword(), nonAdmin.getRole(), nonAdmin.getPermission(), nonAdmin.isEmployed()))))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).changeEmployment(any());

    }
}
