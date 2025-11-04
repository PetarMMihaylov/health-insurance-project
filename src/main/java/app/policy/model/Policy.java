package app.policy.model;

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
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PolicyType policyType;

    @Column(nullable = false)
    private BigDecimal limitForMedications;

    @Column(nullable = false)
    private BigDecimal limitForHospitalTreatment;

    @Column(nullable = false)
    private BigDecimal limitForSurgery;

    @Column(nullable = false)
    private BigDecimal limitForDentalService;

    @Column(nullable = false)
    private BigDecimal policyPrice;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;
}
