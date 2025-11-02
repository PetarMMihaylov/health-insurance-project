package app.transaction.service;

import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.repository.TransactionRepository;
import app.user.model.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}
