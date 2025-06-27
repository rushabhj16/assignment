package com.assignment.backend.dto;

import com.assignment.backend.entity.Customer;

public class CustomerMapper {

    public static Customer toEntity(CustomerRequestDTO dto) {
        return Customer.builder()
                .givenName(dto.getGivenName())
                .middleName(dto.getMiddleName())
                .familyName(dto.getFamilyName())
                .emailAddress(dto.getEmailAddress())
                .contactNumber(dto.getContactNumber())
                .build();
    }

    public static CustomerResponseDTO toDTO(Customer entity) {
        return CustomerResponseDTO.builder()
                .id(entity.getId())
                .givenName(entity.getGivenName())
                .middleName(entity.getMiddleName())
                .familyName(entity.getFamilyName())
                .emailAddress(entity.getEmailAddress())
                .contactNumber(entity.getContactNumber())
                .build();
    }
}
