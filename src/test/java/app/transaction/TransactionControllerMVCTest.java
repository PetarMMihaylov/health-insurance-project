package app.transaction;

import app.exception.DomainException;
import app.exception.TransactionNotFoundException;
import app.security.AuthenticationMetadata;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.service.TransactionService;
import app.user.model.CompanyName;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.TransactionController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private UserService userService;

    private User createDummyUser(UserRole role) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(role.name().toLowerCase() + "@example.com")
                .username(role.name().toLowerCase() + "_user")
                .password("password")
                .firstName(role.name())
                .lastName("User")
                .role(role)
                .permission(role == UserRole.ADMIN ? "can_delete" : "not_delete")
                .companyName(CompanyName.LOCAL_GROUP_LTD)
                .accountBalance(BigDecimal.valueOf(1000))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .employed(true)
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
    void getTransactions_returnsTransactionsForUser() throws Exception {
        User user = createDummyUser(UserRole.POLICYHOLDER);
        AuthenticationMetadata auth = authenticationMetadata(user);

        Transaction t1 = createDummyTransaction(user);
        Transaction t2 = createDummyTransaction(user);

        when(userService.getById(user.getId())).thenReturn(user);
        when(transactionService.getAllTransactions(user)).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/transactions").with(user(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("transactions"));

        verify(userService).getById(user.getId());
        verify(transactionService).getAllTransactions(user);
    }

    @Test
    void getTransactions_noTransactions_redirectsWithFlash() throws Exception {
        User user = createDummyUser(UserRole.POLICYHOLDER);
        AuthenticationMetadata auth = authenticationMetadata(user);

        when(userService.getById(user.getId())).thenReturn(user);
        when(transactionService.getAllTransactions(user))
                .thenThrow(new TransactionNotFoundException("No transactions found"));

        mockMvc.perform(get("/transactions").with(user(auth)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attribute("errorMessage", "No transactions found"));

        verify(transactionService).getAllTransactions(user);
    }

    @Test
    void getTransactionInformation_returnsTransactionForUser() throws Exception {
        User user = createDummyUser(UserRole.POLICYHOLDER);
        AuthenticationMetadata auth = authenticationMetadata(user);

        Transaction transaction = createDummyTransaction(user);

        when(userService.getById(user.getId())).thenReturn(user);
        when(transactionService.getByIdForUser(transaction.getId(), user)).thenReturn(transaction);

        mockMvc.perform(get("/transactions/{id}", transaction.getId()).with(user(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("transaction-details"))
                .andExpect(model().attributeExists("transaction"));

        verify(userService).getById(user.getId());
        verify(transactionService).getByIdForUser(transaction.getId(), user);
    }

    @Test
    void getTransactionInformation_notFound_redirectsWithFlash() throws Exception {
        User user = createDummyUser(UserRole.POLICYHOLDER);
        AuthenticationMetadata auth = authenticationMetadata(user);
        UUID transactionId = UUID.randomUUID();

        when(userService.getById(user.getId())).thenReturn(user);
        when(transactionService.getByIdForUser(transactionId, user))
                .thenThrow(new TransactionNotFoundException("Transaction not found"));

        mockMvc.perform(get("/transactions/{id}", transactionId).with(user(auth)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attribute("errorMessage", "Transaction not found"));
    }

    @Test
    void getTransactionInformation_nonOwnerOrDeleted_redirectsWithFlash() throws Exception {
        User user = createDummyUser(UserRole.POLICYHOLDER);
        AuthenticationMetadata auth = authenticationMetadata(user);

        UUID transactionId = UUID.randomUUID();

        when(userService.getById(user.getId())).thenReturn(user);
        when(transactionService.getByIdForUser(transactionId, user))
                .thenThrow(new DomainException("Access denied for this transaction"));

        mockMvc.perform(get("/transactions/{id}", transactionId).with(user(auth)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attribute("errorMessage", "Access denied for this transaction"));

        verify(transactionService).getByIdForUser(transactionId, user);
    }

    @Test
    void deleteTransaction_adminWithPermission_redirects() throws Exception {
        User admin = createDummyUser(UserRole.ADMIN);
        AuthenticationMetadata auth = authenticationMetadata(admin);

        UUID transactionId = UUID.randomUUID();

        mockMvc.perform(delete("/transactions/{id}/deletion", transactionId)
                        .with(user(auth))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"));

        verify(transactionService).softDelete(transactionId);
    }

    @Test
    void deleteTransaction_nonAdmin_forbidden() throws Exception {
        User user = createDummyUser(UserRole.POLICYHOLDER);
        AuthenticationMetadata auth = authenticationMetadata(user);

        UUID transactionId = UUID.randomUUID();

        mockMvc.perform(delete("/transactions/{id}/deletion", transactionId)
                        .with(user(auth))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(transactionService);
    }

    @Test
    void deleteTransaction_adminWithoutPermission_forbidden() throws Exception {
        User admin = createDummyUser(UserRole.ADMIN);
        admin.setPermission("not_delete");
        AuthenticationMetadata auth = authenticationMetadata(admin);
        UUID transactionId = UUID.randomUUID();

        mockMvc.perform(delete("/transactions/{id}/deletion", transactionId)
                        .with(user(auth))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(transactionService);
    }
}
