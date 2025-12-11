package app.transaction.repository;

import app.transaction.model.Transaction;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findAllByTransactionOwnerAndDeletedFalseOrderByUpdatedOnDesc(User user);

    List<Transaction> findAllByTransactionOwnerAndDeletedFalseAndCreatedOnBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    List<Transaction> findAllByOrderByUpdatedOnDesc();
}
