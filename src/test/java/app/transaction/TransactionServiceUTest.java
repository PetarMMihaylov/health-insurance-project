package app.transaction;

import app.exception.DomainException;
import app.exception.TransactionNotFoundException;
import app.policy.model.Policy;
import app.policy.model.PolicyType;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.repository.TransactionRepository;
import app.transaction.service.TransactionService;
import app.user.model.CompanyName;
import app.user.model.User;
import app.user.model.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceUTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User createDummyUser(UserRole role) {
        Policy dummyPolicy = Policy.builder()
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
                .employed(true)
                .policy(dummyPolicy)
                .build();
    }

    private Transaction createDummyTransaction(User user) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .transactionStatus(TransactionStatus.COMPLETED)
                .referenceNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 8))
                .paidAmount(BigDecimal.valueOf(100))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .deleted(false)
                .transactionOwner(user)
                .build();
    }

    @Test
    void create_createsTransactionSuccessfully() {
        User dummyUser = createDummyUser(UserRole.POLICYHOLDER);
        BigDecimal amount = BigDecimal.valueOf(500);
        TransactionStatus status = TransactionStatus.COMPLETED;

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction transaction = transactionService.create(dummyUser, amount, status);

        assertNotNull(transaction);
        assertEquals(dummyUser, transaction.getTransactionOwner());
        assertEquals(amount, transaction.getPaidAmount());
        assertEquals(status, transaction.getTransactionStatus());
        assertNotNull(transaction.getReferenceNumber());
        assertEquals(8, transaction.getReferenceNumber().length());
        assertFalse(transaction.isDeleted());
        assertNotNull(transaction.getCreatedOn());
        assertNotNull(transaction.getUpdatedOn());

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void getAllTransactions_adminReturnsAllTransactions() {
        User admin = createDummyUser(UserRole.ADMIN);

        Transaction t1 = createDummyTransaction(createDummyUser(UserRole.POLICYHOLDER));
        Transaction t2 = createDummyTransaction(createDummyUser(UserRole.POLICYHOLDER));

        when(transactionRepository.findAll()).thenReturn(List.of(t1, t2));

        List<Transaction> result = transactionService.getAllTransactions(admin);

        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t2));

        verify(transactionRepository, times(1)).findAll();
        verify(transactionRepository, never()).findAllByTransactionOwnerAndDeletedFalse(any());
    }

    @Test
    void getAllTransactions_nonAdminReturnsUserTransactions() {
        User user = createDummyUser(UserRole.POLICYHOLDER);

        Transaction t1 = createDummyTransaction(user);
        Transaction t2 = createDummyTransaction(user);

        when(transactionRepository.findAllByTransactionOwnerAndDeletedFalse(user))
                .thenReturn(List.of(t1, t2));

        List<Transaction> result = transactionService.getAllTransactions(user);

        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t2));

        verify(transactionRepository, times(1)).findAllByTransactionOwnerAndDeletedFalse(user);
        verify(transactionRepository, never()).findAll();
    }

    @Test
    void getAllTransactions_adminNoTransactions_returnsEmptyList() {

        User admin = createDummyUser(UserRole.ADMIN);

        when(transactionRepository.findAll()).thenReturn(List.of());

        List<Transaction> result = transactionService.getAllTransactions(admin);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected an empty list when no transactions are found");

        verify(transactionRepository, times(1)).findAll();
        verify(transactionRepository, never()).findAllByTransactionOwnerAndDeletedFalse(any());
    }


    @Test
    void getAllTransactions_nonAdminNoTransactions_returnsEmptyList() {
        User user = createDummyUser(UserRole.POLICYHOLDER);

        when(transactionRepository.findAllByTransactionOwnerAndDeletedFalse(user)).thenReturn(List.of());

        List<Transaction> result = transactionService.getAllTransactions(user);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected an empty list when no transactions are found");

        verify(transactionRepository, times(1)).findAllByTransactionOwnerAndDeletedFalse(user);
        verify(transactionRepository, never()).findAll();
    }


    @Test
    void getTransactionsCreatedByUserForPeriod_returnsTransactions() {
        User user = createDummyUser(UserRole.POLICYHOLDER);
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();

        Transaction t1 = createDummyTransaction(user);
        Transaction t2 = createDummyTransaction(user);

        when(transactionRepository.findAllByTransactionOwnerAndDeletedFalseAndCreatedOnBetween(
                eq(user),
                eq(start.atStartOfDay()),
                eq(end.atTime(LocalTime.MAX))
        )).thenReturn(List.of(t1, t2));

        List<Transaction> result = transactionService.getTransactionsCreatedByUserForPeriod(user, start, end);

        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t2));

        verify(transactionRepository, times(1))
                .findAllByTransactionOwnerAndDeletedFalseAndCreatedOnBetween(user, start.atStartOfDay(), end.atTime(LocalTime.MAX));
    }

    @Test
    void getTransactionsCreatedByUserForPeriod_noTransactions_returnsEmptyList() {
        User user = createDummyUser(UserRole.POLICYHOLDER);
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();

        when(transactionRepository.findAllByTransactionOwnerAndDeletedFalseAndCreatedOnBetween(any(), any(), any()))
                .thenReturn(List.of());

        List<Transaction> result = transactionService.getTransactionsCreatedByUserForPeriod(user, start, end);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected an empty list when no transactions are found");

        verify(transactionRepository, times(1))
                .findAllByTransactionOwnerAndDeletedFalseAndCreatedOnBetween(any(), any(), any());
    }

    @Test
    void getById_transactionExists_returnsTransaction() {
        Transaction t = createDummyTransaction(createDummyUser(UserRole.ADMIN));
        when(transactionRepository.findById(t.getId())).thenReturn(Optional.of(t));

        Transaction result = transactionService.getById(t.getId());

        assertEquals(t, result);
        verify(transactionRepository, times(1)).findById(t.getId());
    }

    @Test
    void getById_transactionNotFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        TransactionNotFoundException ex = assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getById(id));

        assertEquals("No such transaction has been found", ex.getMessage());
        verify(transactionRepository, times(1)).findById(id);
    }

    @Test
    void getByIdForUser_adminAccess_returnsTransaction() {
        User admin = createDummyUser(UserRole.ADMIN);
        Transaction t = createDummyTransaction(createDummyUser(UserRole.POLICYHOLDER));
        when(transactionRepository.findById(t.getId())).thenReturn(Optional.of(t));

        Transaction result = transactionService.getByIdForUser(t.getId(), admin);

        assertEquals(t, result);
        verify(transactionRepository, times(1)).findById(t.getId());
    }

    @Test
    void getByIdForUser_ownerAccess_returnsTransaction() {
        User user = createDummyUser(UserRole.POLICYHOLDER);
        Transaction t = createDummyTransaction(user);
        when(transactionRepository.findById(t.getId())).thenReturn(Optional.of(t));

        Transaction result = transactionService.getByIdForUser(t.getId(), user);

        assertEquals(t, result);
        verify(transactionRepository, times(1)).findById(t.getId());
    }

    @Test
    void getByIdForUser_nonOwner_throwsDomainException() {
        User user = createDummyUser(UserRole.POLICYHOLDER);
        User other = createDummyUser(UserRole.POLICYHOLDER);
        Transaction t = createDummyTransaction(other);
        when(transactionRepository.findById(t.getId())).thenReturn(Optional.of(t));

        DomainException ex = assertThrows(DomainException.class,
                () -> transactionService.getByIdForUser(t.getId(), user));

        assertEquals("Access denied for this transaction", ex.getMessage());
        verify(transactionRepository, times(1)).findById(t.getId());
    }

    @Test
    void getByIdForUser_deletedTransaction_throwsDomainException() {
        User user = createDummyUser(UserRole.POLICYHOLDER);
        Transaction t = createDummyTransaction(user);
        t.setDeleted(true);
        when(transactionRepository.findById(t.getId())).thenReturn(Optional.of(t));

        DomainException ex = assertThrows(DomainException.class,
                () -> transactionService.getByIdForUser(t.getId(), user));

        assertEquals("Access denied for this transaction", ex.getMessage());
        verify(transactionRepository, times(1)).findById(t.getId());
    }

    @Test
    void softDelete_transactionTogglesDeleted() {
        Transaction t = createDummyTransaction(createDummyUser(UserRole.POLICYHOLDER));
        t.setDeleted(false);

        when(transactionRepository.findById(t.getId())).thenReturn(Optional.of(t));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.softDelete(t.getId());

        assertTrue(t.isDeleted());
        verify(transactionRepository, times(1)).findById(t.getId());
        verify(transactionRepository, times(1)).save(t);
    }

    @Test
    void softDelete_transactionNotFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        TransactionNotFoundException ex = assertThrows(TransactionNotFoundException.class,
                () -> transactionService.softDelete(id));

        assertEquals("No such transaction has been found", ex.getMessage());
        verify(transactionRepository, times(1)).findById(id);
    }
}
