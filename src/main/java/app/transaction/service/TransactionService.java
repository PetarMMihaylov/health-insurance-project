package app.transaction.service;

import app.exception.DomainException;
import app.exception.TransactionNotFoundException;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.repository.TransactionRepository;
import app.user.model.User;
import app.user.model.UserRole;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction create(User user, BigDecimal amount, TransactionStatus status) {

        Transaction transaction = Transaction.builder()
                .transactionStatus(status)
                .referenceNumber(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8))
                .paidAmount(amount)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .deleted(false)
                .transactionOwner(user)
                .build();

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsCreatedByUserForPeriod(User user, LocalDate startDate, LocalDate endDate) {

        List<Transaction> transactions = transactionRepository.findAllByTransactionOwnerAndDeletedFalseAndCreatedOnBetween(user, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException("No transactions found for user with id " + user.getId());
        }

        return transactions;
    }

    public List<Transaction> getAllTransactions(User user) {

        List<Transaction> transactions;
        if (user.getRole() == UserRole.ADMIN) {
            transactions = transactionRepository.findAll();
        } else {
            transactions = transactionRepository.findAllByTransactionOwnerAndDeletedFalse(user);
        }

        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException("No transactions found for user with id " + user.getId());
        }

        return transactions;
    }

    public Transaction getById(UUID id) {
        return transactionRepository.findById(id).orElseThrow(() -> new TransactionNotFoundException("No such transaction has been found"));
    }

    public Transaction getByIdForUser(UUID id, User user) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        if (user.getRole() != UserRole.ADMIN) {
            if (!transaction.getTransactionOwner().equals(user) || transaction.isDeleted()) {
                throw new DomainException("Access denied for this transaction");
            }
        }

        return transaction;
    }

    public void softDelete(UUID id) {
        Transaction transaction = getById(id);
        transaction.setDeleted(!transaction.isDeleted());
        transaction.setUpdatedOn(LocalDateTime.now());
        transactionRepository.save(transaction);
    }
}
