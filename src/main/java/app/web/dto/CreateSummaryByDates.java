package app.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSummaryByDates {

    @NotNull(message = "Field cannot be empty!")
    private LocalDate startDate;

    @NotNull(message = "Field cannot be empty!")
    private LocalDate endDate;
}
