package app.user.service;

import app.exception.InvalidCompanyException;
import app.exception.UserAlreadyFoundException;
import app.exception.UserNotFoundException;
import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.policy.service.PolicyService;
import app.security.AuthenticationMetadata;
import app.transaction.model.TransactionStatus;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.web.dto.AccountBalanceRequest;
import app.web.dto.ProfileEditRequest;
import app.web.dto.RegisterRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PolicyService policyService;
    private final TransactionService transactionService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, PolicyService policyService, TransactionService transactionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.policyService = policyService;
        this.transactionService = transactionService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        return new AuthenticationMetadata(user.getId(), username, user.getPassword(), user.getRole(), user.getPermission(), user.isEmployed());
    }

    @CacheEvict(value = "users", allEntries = true)
    public void register(RegisterRequest registerRequest) {

        Optional<User> optionalUser = userRepository.findByUsername(registerRequest.getUsername());
        if (optionalUser.isPresent()) {
            throw new UserAlreadyFoundException("User with username [%s] is already present.".formatted(registerRequest.getUsername()));
        }

        Policy correctPolicy;
        UserRole role;
        String permission;

        if (registerRequest.getCompany().getDisplayName().equals("Local Group Ltd.")) {
            correctPolicy = policyService.getByType(PolicyType.COMFORT);
            role = UserRole.POLICYHOLDER;
            permission = "not_delete";
        } else if (registerRequest.getCompany().getDisplayName().equals("Neuro Nest")) {
            correctPolicy = policyService.getByType(PolicyType.STANDARD);
            role = UserRole.POLICYHOLDER;
            permission = "not_delete";
        } else if (registerRequest.getCompany().getDisplayName().equals("Solar Bloom")) {
            correctPolicy = policyService.getByType(PolicyType.LUX);
            role = UserRole.POLICYHOLDER;
            permission = "not_delete";
        } else if (registerRequest.getCompany().getDisplayName().equals("Health Insurance Inc.")) {
            correctPolicy = policyService.getByType(PolicyType.LUX);
            role = UserRole.ADMIN;
            permission = "can_delete";
        } else {
            throw new InvalidCompanyException("Invalid company name: " + registerRequest.getCompany().getDisplayName());
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .role(role)
                .permission(permission)
                .companyName(registerRequest.getCompany())
                .employed(true)
                .policy(correctPolicy)
                .accountBalance(BigDecimal.ZERO)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        userRepository.save(user);

        log.info("User [{}] registered with role [{}] and policy [{}].",
                registerRequest.getUsername(), role, correctPolicy.getPolicyType().getDisplayName());
    }

    public User getById(UUID id) {

        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with [%s] id is not present.".formatted(id)));
    }

    @CacheEvict(value = "users", allEntries = true)
    public void updateProfile(UUID id, ProfileEditRequest profileEditRequest) {

        User user = getById(id);

        user.setFirstName(profileEditRequest.getFirstName());
        user.setLastName(profileEditRequest.getLastName());
        user.setEmail(profileEditRequest.getEmail());
        user.setProfilePicture(profileEditRequest.getProfilePicture());
        user.setUpdatedOn(LocalDateTime.now());

        userRepository.save(user);

        log.info("Updated profile for user {}. New name: {} {}, email: {}",
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail());
    }

    @CacheEvict(value = "users", allEntries = true)
    public void changeRole(UUID id) {
        User user = getById(id);

        if (user.getRole() == UserRole.ADMIN) {
            user.setRole(UserRole.POLICYHOLDER);
        } else {
            user.setRole(UserRole.ADMIN);
        }

        user.setUpdatedOn(LocalDateTime.now());
        userRepository.save(user);

        log.info("Changed role for user {}. New role: {}", user.getUsername(), user.getRole());
    }

    @CacheEvict(value = "users", allEntries = true)
    public void changeEmployment(UUID id) {
        User user = getById(id);
        user.setEmployed(!user.isEmployed());
        user.setUpdatedOn(LocalDateTime.now());
        userRepository.save(user);

        log.info("Changed employment status for user {}. Employed: {}", user.getUsername(), user.isEmployed());
    }

    @Cacheable("users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void updateBalance(UUID id, AccountBalanceRequest accountBalanceRequest) {
        User user = getById(id);
        BigDecimal amountToIncrease = accountBalanceRequest.getAddedAmount();
        user.setAccountBalance(user.getAccountBalance().add(amountToIncrease));
        user.setUpdatedOn(LocalDateTime.now());
        userRepository.save(user);
        transactionService.create(user, amountToIncrease, TransactionStatus.COMPLETED);

        log.info("Updated balance for user {}. Added amount: {}, new balance: {}",
                user.getUsername(),
                amountToIncrease,
                user.getAccountBalance());
    }

    @CacheEvict(value = "users", allEntries = true)
    public boolean changePolicy(UUID id, User user) {
        Policy policy = policyService.getById(id);

        if (user.getAccountBalance().compareTo(policy.getPolicyPrice()) >= 0) {
            user.setAccountBalance(user.getAccountBalance().subtract(policy.getPolicyPrice()));
            user.setPolicy(policy);
            user.setUpdatedOn(LocalDateTime.now());
            userRepository.save(user);
            transactionService.create(user, policy.getPolicyPrice(), TransactionStatus.COMPLETED);
            log.info("User [{}] changed policy to [{}].", user.getUsername(), policy.getPolicyType().getDisplayName());
            return true;
        } else {
            transactionService.create(user, policy.getPolicyPrice(), TransactionStatus.FAILED);
            log.info("User [{}] unsuccessfully tried to change policy to [{}].", user.getUsername(), policy.getPolicyType().getDisplayName());
            return false;
        }
    }

    public void persistUser(User user) {
        userRepository.save(user);
    }
}
