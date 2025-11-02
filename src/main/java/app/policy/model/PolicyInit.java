package app.policy.model;

import app.policy.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class PolicyInit implements ApplicationRunner {

    private static final BigDecimal LIMIT_FOR_MEDICATIONS_COMFORT = BigDecimal.valueOf(200);
    private static final BigDecimal LIMIT_FOR_HOSPITAL_TREATMENT_COMFORT = BigDecimal.valueOf(1000);
    private static final BigDecimal LIMIT_FOR_SURGERY_COMFORT = BigDecimal.valueOf(0);
    private static final BigDecimal LIMIT_FOR_DENTAL_SERVICE_COMFORT = BigDecimal.valueOf(0);
    private static final BigDecimal LIMIT_FOR_MEDICATIONS_STANDARD = BigDecimal.valueOf(300);
    private static final BigDecimal LIMIT_FOR_HOSPITAL_TREATMENT_STANDARD = BigDecimal.valueOf(2000);
    private static final BigDecimal LIMIT_FOR_SURGERY_STANDARD = BigDecimal.valueOf(1500);
    private static final BigDecimal LIMIT_FOR_DENTAL_SERVICE_STANDARD = BigDecimal.valueOf(500);
    private static final BigDecimal LIMIT_FOR_MEDICATIONS_LUX = BigDecimal.valueOf(500);
    private static final BigDecimal LIMIT_FOR_HOSPITAL_TREATMENT_LUX = BigDecimal.valueOf(5000);
    private static final BigDecimal LIMIT_FOR_SURGERY_LUX = BigDecimal.valueOf(3000);
    private static final BigDecimal LIMIT_FOR_DENTAL_SERVICE_LUX = BigDecimal.valueOf(1000);
    private final PolicyRepository policyRepository;

    @Autowired
    public PolicyInit(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (policyRepository.count() == 0) {
            policyRepository.saveAll(List.of(
                    Policy.builder()
                            .policyType(PolicyType.COMFORT)
                            .limitForMedications(LIMIT_FOR_MEDICATIONS_COMFORT)
                            .limitForHospitalTreatment(LIMIT_FOR_HOSPITAL_TREATMENT_COMFORT)
                            .limitForSurgery(LIMIT_FOR_SURGERY_COMFORT)
                            .limitForDentalService(LIMIT_FOR_DENTAL_SERVICE_COMFORT)
                            .createdOn(LocalDateTime.now())
                            .build(),
                    Policy.builder()
                            .policyType(PolicyType.STANDARD)
                            .limitForMedications(LIMIT_FOR_MEDICATIONS_STANDARD)
                            .limitForHospitalTreatment(LIMIT_FOR_HOSPITAL_TREATMENT_STANDARD)
                            .limitForSurgery(LIMIT_FOR_SURGERY_STANDARD)
                            .limitForDentalService(LIMIT_FOR_DENTAL_SERVICE_STANDARD)
                            .createdOn(LocalDateTime.now())
                            .build(),
                    Policy.builder()
                            .policyType(PolicyType.LUX)
                            .limitForMedications(LIMIT_FOR_MEDICATIONS_LUX)
                            .limitForHospitalTreatment(LIMIT_FOR_HOSPITAL_TREATMENT_LUX)
                            .limitForSurgery(LIMIT_FOR_SURGERY_LUX)
                            .limitForDentalService(LIMIT_FOR_DENTAL_SERVICE_LUX)
                            .createdOn(LocalDateTime.now())
                            .build()));
        }
    }
}
