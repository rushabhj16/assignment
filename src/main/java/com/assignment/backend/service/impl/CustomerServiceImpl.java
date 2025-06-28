package com.assignment.backend.service.impl;

import com.assignment.backend.entity.Customer;
import com.assignment.backend.exception.CustomerNotFoundException;
import com.assignment.backend.exception.DuplicateEmailException;
import com.assignment.backend.repository.CustomerRepository;
import com.assignment.backend.service.CustomerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the CustomerService interface.
 * Handles business logic for creating, updating, retrieving, and deleting customers.
 */

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;

    public CustomerServiceImpl(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }

    @Override
    public Optional<Customer> getCustomerById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Customer> getCustomerByEmail(String email) {
        return repository.findByEmailAddress(email);
    }

    @Override
    public Customer createCustomer(Customer customer) {
        customer.setEmailAddress(customer.getEmailAddress().toLowerCase().trim());

        if (repository.existsByEmailAddress(customer.getEmailAddress())) {
            throw new DuplicateEmailException("Email already in use: " + customer.getEmailAddress());
        }

        return repository.save(customer);
    }

    @Override
    public Customer updateCustomer(UUID id, Customer updated) {
        updated.setEmailAddress(updated.getEmailAddress().toLowerCase().trim());

        return repository.findById(id).map(existing -> {
            if (!existing.getEmailAddress().equals(updated.getEmailAddress()) &&
                    repository.existsByEmailAddress(updated.getEmailAddress())) {
                throw new DuplicateEmailException("Email already in use: " + updated.getEmailAddress());
            }

            existing.setGivenName(updated.getGivenName());
            existing.setMiddleName(updated.getMiddleName());
            existing.setFamilyName(updated.getFamilyName());
            existing.setContactNumber(updated.getContactNumber());
            existing.setEmailAddress(updated.getEmailAddress());

            return repository.save(existing);
        }).orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));
    }

    @Override
    public void deleteCustomer(UUID id) {
        if (!repository.existsById(id)) {
            throw new CustomerNotFoundException("Customer not found with ID: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
