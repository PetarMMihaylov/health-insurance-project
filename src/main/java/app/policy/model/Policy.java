package app.policy.model;

import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    private LocalDateTime createdOn;
}
