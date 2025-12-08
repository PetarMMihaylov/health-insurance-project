package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileEditRequest {

    @NotBlank(message = "First name field must not be empty.")
    @Size(min = 2, max = 18, message = "First name must be between 2 and 18 symbols.")
    private String firstName;

    @NotBlank(message = "First name field must not be empty.")
    @Size(min = 2, max = 18, message = "First name must be between 2 and 18 symbols.")
    private String lastName;

    @Email
    private String email;

    @URL
    private String profilePicture;
}
