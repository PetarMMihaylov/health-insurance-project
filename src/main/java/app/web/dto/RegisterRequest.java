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

    @NotBlank
    @Size(min = 3, max = 16, message = "Username length must be between 3 and 16 symbols.")
    private String username;

    @NotBlank
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 symbols.")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$", message = "Password must contain at least one uppercase letter and one special symbol.")
    private String password;

    @NotBlank
    @Email(message = "Please provide a valid email address.")
    private String email;

    @NotBlank
    @Size(min = 3, max = 18)
    private String firstName;

    @NotBlank
    @Size(min = 3, max = 18)
    private String lastName;

    @NotNull(message = "Company must be selected.")
    private CompanyName company;
}
