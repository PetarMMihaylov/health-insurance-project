package app.user;

import app.exception.UserNotFoundException;
import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.model.CompanyName;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.ProfileEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User buildTestUser(UUID id, UserRole role) {
        return User.builder()
                .id(id)
                .username("user123")
                .password("Password@1")
                .email("user123@example.com")
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
    void getById_existingUser_returnsUser() {

        UUID userId = UUID.randomUUID();
        User user = buildTestUser(userId, UserRole.POLICYHOLDER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getById_nonExistingUser_throwsException() {

        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getById(userId));
        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void updateProfile_validRequest_updatesUser() {

        UUID userId = UUID.randomUUID();
        User user = buildTestUser(userId, UserRole.POLICYHOLDER);

        ProfileEditRequest request = ProfileEditRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .profilePicture("http://example.com/profile.jpg")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateProfile(userId, request);

        assertEquals("Alice", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("http://example.com/profile.jpg", user.getProfilePicture());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changeRole_switchesPolicyholderToAdmin() {

        UUID userId = UUID.randomUUID();
        User user = buildTestUser(userId, UserRole.POLICYHOLDER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changeRole(userId);

        assertEquals(UserRole.ADMIN, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changeRole_switchesAdminToPolicyholder() {

        UUID userId = UUID.randomUUID();
        User user = buildTestUser(userId, UserRole.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changeRole(userId);

        assertEquals(UserRole.POLICYHOLDER, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changeEmployment_togglesEmployment() {

        UUID userId = UUID.randomUUID();
        User user = buildTestUser(userId, UserRole.POLICYHOLDER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        boolean originalStatus = user.isEmployed();

        userService.changeEmployment(userId);

        assertEquals(!originalStatus, user.isEmployed());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void getAllUsers_returnsAllUsers() {

        UUID userId = UUID.randomUUID();
        User user = buildTestUser(userId, UserRole.POLICYHOLDER);

        List<User> users = List.of(user);
        when(userRepository.findAllByOrderByUpdatedOnDesc()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(users, result);
        verify(userRepository, times(1)).findAllByOrderByUpdatedOnDesc();
    }

    @Test
    void persistUser_savesUser() {

        UUID userId = UUID.randomUUID();
        User user = buildTestUser(userId, UserRole.POLICYHOLDER);

        userService.persistUser(user);

        verify(userRepository, times(1)).save(user);
    }
}
