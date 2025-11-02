package app.transaction.model;

import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Column(nullable = false)
    private String referenceNumber;

    @Column(nullable = false)
    private BigDecimal paidAmount;

    private String details;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @ManyToOne(optional = false)
    private User transactionOwner;
}
