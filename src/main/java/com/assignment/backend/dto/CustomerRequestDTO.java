package com.assignment.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequestDTO {

    @NotBlank(message = "Given name is required")
    private String givenName;

    @Size(max = 50)
    private String middleName;

    @NotBlank(message = "Family name is required")
    private String familyName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Email address must be valid")
    private String emailAddress;

    @NotBlank(message = "Contact number is required")
    @Pattern(
            regexp = "^\\+?[1-9][0-9]{6,14}$",
            message = "Contact number must be 7 to 15 digits, optionally starting with +"
    )
    private String contactNumber;
}
