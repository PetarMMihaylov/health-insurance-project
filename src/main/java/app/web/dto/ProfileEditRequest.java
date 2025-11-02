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

    @Size(min = 3, max = 18)
    private String firstName;

    @Size(min = 3, max = 18)
    private String lastName;


    @Email(message = "Please provide a valid email address.")
    private String email;

    @URL
    private String profilePicture;
}
