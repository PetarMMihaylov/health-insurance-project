package app.web.dto;


import app.user.model.CompanyName;
import jakarta.validation.constraints.*;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username field must not be empty.")
    @Size(min = 3, max = 16, message = "Username length must be between 3 and 16 symbols.")
    private String username;

    @NotBlank(message = "Password field must not be empty.")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 symbols.")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$", message = "Password must contain at least one uppercase letter and one special symbol.")
    private String password;

    @Email
    private String email;

    @NotBlank(message = "First name field must not be empty.")
    @Size(min = 2, max = 18, message = "First name must be between 2 and 18 symbols.")
    private String firstName;

    @NotBlank(message = "Last name field must not be empty.")
    @Size(min = 2, max = 18, message = "Last name must be between 2 and 18 symbols.")
    private String lastName;

    @NotNull(message = "Company must be selected.")
    private CompanyName company;
}
