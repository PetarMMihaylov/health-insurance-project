package app.transaction.model;

import app.claim.model.Claim;
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

    @Column(nullable = false)
    private String paidUser;

    private String details;

    @Column(nullable = false)
    private LocalDateTime createdOn;
}
