package com.assignment.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entity representing a Customer.
 * Mapped to the 'customers' table with constraints for uniqueness and nullability.
 */
@Entity
@Table(
        name = "customers",
        uniqueConstraints = @UniqueConstraint(columnNames = "email_address")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Customer {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "given_name", nullable = false)
    private String givenName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "family_name", nullable = false)
    private String familyName;

    @Column(name = "email_address", nullable = false, unique = true)
    private String emailAddress;

    @Column(name = "contact_number", nullable = false)
    private String contactNumber;
}
