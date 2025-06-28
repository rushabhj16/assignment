package com.assignment.backend.controller;

import com.assignment.backend.dto.CustomerMapper;
import com.assignment.backend.dto.CustomerRequestDTO;
import com.assignment.backend.dto.CustomerResponseDTO;
import com.assignment.backend.entity.Customer;
import com.assignment.backend.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing Customer entities.
 * Provides CRUD operations and additional HTTP support methods.
 */

@Tag(name = "Customer Controller", description = "CRUD operations for customers")
@RestController
@RequestMapping("/api/customers")
@Validated
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @Operation(summary = "Get all customers")
    @GetMapping
    public List<CustomerResponseDTO> getAllCustomers() {
        return service.getAllCustomers().stream()
                .map(CustomerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Get customer by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable UUID id) {
        return service.getCustomerById(id)
                .map(CustomerMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get customer by email address")
    @GetMapping("/search")
    public ResponseEntity<CustomerResponseDTO> getCustomerByEmail(@RequestParam String email) {
        return service.getCustomerByEmail(email)
                .map(CustomerMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new customer")
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@RequestBody @Valid CustomerRequestDTO requestDTO) {
        Customer created = service.createCustomer(CustomerMapper.toEntity(requestDTO));
        URI location = URI.create("/api/customers/" + created.getId());
        return ResponseEntity.created(location).body(CustomerMapper.toDTO(created));
    }

    @Operation(summary = "Update an existing customer")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable UUID id,
            @RequestBody @Valid CustomerRequestDTO requestDTO) {

        Customer incoming = CustomerMapper.toEntity(requestDTO);
        incoming.setId(id);
        Customer updated = service.updateCustomer(id, incoming);
        return ResponseEntity.ok(CustomerMapper.toDTO(updated));
    }

    @Operation(summary = "Delete a customer")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        service.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Partially update a customer's contact number")
    @PatchMapping("/{id}/contact")
    public ResponseEntity<CustomerResponseDTO> updateContact(
            @PathVariable UUID id,
            @RequestParam
            @NotBlank(message = "Contact number is required")
            @Pattern(
                    regexp = "^\\+?[1-9][0-9]{6,14}$",
                    message = "Contact number must be 7 to 15 digits, optionally starting with +"
            )
            String contactNumber) {

        return service.getCustomerById(id).map(customer -> {
            customer.setContactNumber(contactNumber);
            Customer updated = service.updateCustomer(id, customer);
            return ResponseEntity.ok(CustomerMapper.toDTO(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Check if a customer exists by ID")
    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExists(@PathVariable UUID id) {
        return service.getCustomerById(id).isPresent()
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "List all allowed HTTP methods for /api/customers")
    @RequestMapping(method = RequestMethod.OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> options() {
        return ResponseEntity.ok()
                .allow(
                        HttpMethod.GET,
                        HttpMethod.POST,
                        HttpMethod.PUT,
                        HttpMethod.PATCH,
                        HttpMethod.DELETE,
                        HttpMethod.OPTIONS,
                        HttpMethod.HEAD
                )
                .build();
    }
}
