package app.transaction.model;

import app.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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

    @Column(nullable = false, unique = true, length = 8)
    @Size(min = 8, max = 8)
    private String referenceNumber;

    @Column(nullable = false)
    private BigDecimal paidAmount;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @Column(nullable = false)
    private boolean deleted;

    @ManyToOne(optional = false)
    private User transactionOwner;
}
