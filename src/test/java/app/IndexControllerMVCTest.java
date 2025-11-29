package app;

import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.security.AuthenticationMetadata;
import app.user.model.CompanyName;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.IndexController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
class IndexControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getIndexPage_ShouldReturnIndexView() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getRegisterPage_ShouldReturnRegisterViewWithEmptyModel() throws Exception {

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void register_ValidRequest_ShouldRedirectToLogin() throws Exception {

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "JohnDoe")
                        .param("password", "Strong@123")
                        .param("email", "john@example.com")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("company", CompanyName.LOCAL_GROUP_LTD.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).register(any());
    }

    @Test
    void register_InvalidRequest_ShouldReturnValidationErrors() throws Exception {

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "jo")
                        .param("password", "weakpw")
                        .param("email", "not-an-email")
                        .param("firstName", "Jo")
                        .param("lastName", "")
                        .param("company", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors(
                        "registerRequest",
                        "username",
                        "password",
                        "email",
                        "firstName",
                        "lastName",
                        "company"
                ));

        verify(userService, never()).register(any());
    }

    @Test
    void getLoginPage_ShouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void getLoginPage_WithError_ShouldInjectErrorMessage() throws Exception {
        mockMvc.perform(get("/login").param("error", "error"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"))
                .andExpect(model().attribute("error",
                        "Invalid username or password"));
    }

    @Test
    void getHomePage_ShouldReturnHomeViewAndUser() throws Exception {

        UUID userId = UUID.randomUUID();

        AuthenticationMetadata authenticationMetadata = new AuthenticationMetadata(
                userId,
                "john",
                "Password@1",
                UserRole.POLICYHOLDER,
                "not_delete",
                true
        );

        Policy policy = Policy.builder()
                .id(UUID.randomUUID())
                .policyType(PolicyType.STANDARD)
                .limitForMedications(BigDecimal.valueOf(100))
                .limitForHospitalTreatment(BigDecimal.valueOf(1000))
                .limitForSurgery(BigDecimal.valueOf(1500))
                .limitForDentalService(BigDecimal.valueOf(500))
                .policyPrice(BigDecimal.valueOf(200))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        User user = User.builder()
                .id(userId)
                .email("john@example.com")
                .username("john")
                .password("Password@1")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.POLICYHOLDER)
                .permission("not_delete")
                .companyName(CompanyName.LOCAL_GROUP_LTD)
                .accountBalance(BigDecimal.TEN)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .policy(policy)
                .employed(true)
                .build();

        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(get("/home").with(user(authenticationMetadata)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", user));

    }

    @Test
    void getHomePage_NoAuthentication_ShouldRedirectToLogin() throws Exception {

        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

    }
}
