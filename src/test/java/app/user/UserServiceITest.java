package app.user;

import app.exception.UserAlreadyFoundException;
import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.policy.service.PolicyService;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.repository.TransactionRepository;
import app.transaction.service.TransactionService;
import app.user.model.CompanyName;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.AccountBalanceRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void register_validUser_savesUserWithCorrectPolicyAndRole() {

        Policy standardPolicy = policyService.getByType(PolicyType.STANDARD);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser1");
        request.setPassword("Password@1");
        request.setEmail("testuser1@example.com");
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setCompany(CompanyName.NEURO_NEST);

        userService.register(request);

        Optional<User> saved = userRepository.findByUsername("testuser1");
        assertTrue(saved.isPresent());
        User user = saved.get();
        assertEquals(UserRole.POLICYHOLDER, user.getRole());
        assertEquals(standardPolicy.getId(), user.getPolicy().getId());
        assertEquals(BigDecimal.ZERO, user.getAccountBalance());
    }

    @Test
    void register_existingUsername_throwsException() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");
        request.setPassword("Password@1");
        request.setEmail("existing@example.com");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setCompany(CompanyName.NEURO_NEST);

        userService.register(request);

        assertThrows(UserAlreadyFoundException.class, () -> userService.register(request));
    }

    @Test
    void updateBalance_addsAmountAndCreatesTransaction() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("balanceUser");
        request.setPassword("Password@1");
        request.setEmail("balance@example.com");
        request.setFirstName("Bob");
        request.setLastName("Builder");
        request.setCompany(CompanyName.NEURO_NEST);

        userService.register(request);
        User user = userRepository.findByUsername("balanceUser").get();

        BigDecimal addAmount = BigDecimal.valueOf(500);
        AccountBalanceRequest balanceRequest = new AccountBalanceRequest();
        balanceRequest.setAddedAmount(addAmount);

        userService.updateBalance(user.getId(), balanceRequest);

        User updatedUser = userRepository.findById(user.getId()).get();
        assertEquals(addAmount, updatedUser.getAccountBalance());

        List<Transaction> transactions = transactionRepository.findAllByTransactionOwnerAndDeletedFalse(user);
        assertEquals(1, transactions.size());
        Transaction tx = transactions.get(0);
        assertEquals(addAmount, tx.getPaidAmount());
        assertEquals(TransactionStatus.COMPLETED, tx.getTransactionStatus());
    }

    @Test
    void changePolicy_sufficientBalance_updatesPolicyAndCreatesTransaction() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("policyUser");
        request.setPassword("Password@1");
        request.setEmail("policy@example.com");
        request.setFirstName("Charlie");
        request.setLastName("Chocolate");
        request.setCompany(CompanyName.NEURO_NEST);

        userService.register(request);
        User user = userRepository.findByUsername("policyUser").get();

        Policy luxPolicy = policyService.getByType(PolicyType.LUX);

        BigDecimal initialBalance = luxPolicy.getPolicyPrice().add(BigDecimal.valueOf(100));
        user.setAccountBalance(initialBalance);
        userRepository.save(user);

        userService.changePolicy(luxPolicy.getId(), user);

        User updatedUser = userRepository.findById(user.getId()).get();
        assertEquals(luxPolicy.getId(), updatedUser.getPolicy().getId());

        BigDecimal expectedBalance = initialBalance.subtract(luxPolicy.getPolicyPrice());
        assertEquals(expectedBalance, updatedUser.getAccountBalance());

        List<Transaction> transactions = transactionRepository.findAllByTransactionOwnerAndDeletedFalse(user);
        assertEquals(1, transactions.size());
        Transaction tx = transactions.get(0);
        assertEquals(luxPolicy.getPolicyPrice(), tx.getPaidAmount());
        assertEquals(TransactionStatus.COMPLETED, tx.getTransactionStatus());
    }

    @Test
    void changePolicy_insufficientBalance_transactionFailsAndPolicyNotChanged() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("poorUser");
        request.setPassword("Password@1");
        request.setEmail("poor@example.com");
        request.setFirstName("Dave");
        request.setLastName("Daring");
        request.setCompany(CompanyName.NEURO_NEST);

        userService.register(request);
        User user = userRepository.findByUsername("poorUser").get();

        Policy luxPolicy = policyService.getByType(PolicyType.LUX);

        userService.changePolicy(luxPolicy.getId(), user);

        User updatedUser = userRepository.findById(user.getId()).get();
        assertNotEquals(luxPolicy.getId(), updatedUser.getPolicy().getId());
        assertEquals(BigDecimal.ZERO, updatedUser.getAccountBalance());

        List<Transaction> transactions = transactionRepository.findAllByTransactionOwnerAndDeletedFalse(user);
        assertEquals(1, transactions.size());
        Transaction tx = transactions.get(0);
        assertEquals(luxPolicy.getPolicyPrice(), tx.getPaidAmount());
        assertEquals(TransactionStatus.FAILED, tx.getTransactionStatus());
    }
}


