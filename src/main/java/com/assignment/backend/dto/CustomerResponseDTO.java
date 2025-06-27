package com.assignment.backend.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponseDTO {
    private UUID id;
    private String givenName;
    private String middleName;
    private String familyName;
    private String emailAddress;
    private String contactNumber;
}