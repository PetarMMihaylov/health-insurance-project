package app.web.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank
    @Size(min = 3, max = 16, message = "Username length must be between 3 and 16 symbols.")
    private String username;

    @NotBlank
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 symbols.")
    private String password;

}