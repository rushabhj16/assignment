package com.assignment.backend.service;

import com.assignment.backend.entity.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface defining the contract for Customer-related operations.
 */

public interface CustomerService {

    List<Customer> getAllCustomers();

    Optional<Customer> getCustomerById(UUID id);

    Optional<Customer> getCustomerByEmail(String email);

    Customer createCustomer(Customer customer);

    Customer updateCustomer(UUID id, Customer updated);

    void deleteCustomer(UUID id);

    boolean existsById(UUID id);
}
