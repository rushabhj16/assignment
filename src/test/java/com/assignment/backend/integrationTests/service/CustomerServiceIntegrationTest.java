package com.assignment.backend.integrationTests.service;

import com.assignment.backend.entity.Customer;
import com.assignment.backend.exception.CustomerNotFoundException;
import com.assignment.backend.exception.DuplicateEmailException;
import com.assignment.backend.service.CustomerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("acceptance")
@SpringBootTest
class CustomerServiceIntegrationTest {

    @Autowired
    private CustomerService service;

    private Customer baseCustomer;

    @BeforeEach
    void setUp() {
        baseCustomer = Customer.builder()
                .givenName("Alice")
                .middleName("M")
                .familyName("Walker")
                .contactNumber("+1234567890")
                .build();
    }

    @Test
    void shouldCreateAndRetrieveCustomer() {
        Customer customer = baseCustomer.toBuilder()
                .emailAddress("test.create@example.com")
                .build();

        Customer created = service.createCustomer(customer);
        assertNotNull(created.getId());

        Customer found = service.getCustomerById(created.getId())
                .orElseThrow(() -> new AssertionError("Customer not found"));

        assertEquals("test.create@example.com", found.getEmailAddress());
    }

    @Test
    void shouldNormalizeEmailOnCreate() {
        Customer customer = baseCustomer.toBuilder()
                .emailAddress("  Normalize@Example.COM ")
                .build();

        Customer created = service.createCustomer(customer);
        assertEquals("normalize@example.com", created.getEmailAddress());
    }

    @Test
    void shouldThrowOnDuplicateEmail() {
        Customer customer1 = baseCustomer.toBuilder()
                .emailAddress("duplicate@example.com")
                .build();
        Customer customer2 = baseCustomer.toBuilder()
                .emailAddress("duplicate@example.com")
                .build();

        service.createCustomer(customer1);
        assertThrows(DuplicateEmailException.class, () -> service.createCustomer(customer2));
    }

    @Test
    void shouldUpdateCustomerDetails() {
        Customer original = baseCustomer.toBuilder()
                .emailAddress("update.success@example.com")
                .build();
        Customer created = service.createCustomer(original);

        Customer update = original.toBuilder()
                .givenName("Updated")
                .contactNumber("+1987654321")
                .build();

        Customer result = service.updateCustomer(created.getId(), update);

        assertEquals("Updated", result.getGivenName());
        assertEquals("+1987654321", result.getContactNumber());
    }

    @Test
    void shouldThrowIfUpdatedEmailAlreadyExists() {
        Customer one = baseCustomer.toBuilder().emailAddress("first@example.com").build();
        Customer two = baseCustomer.toBuilder().emailAddress("second@example.com").build();

        service.createCustomer(one);
        Customer createdTwo = service.createCustomer(two);

        Customer conflict = createdTwo.toBuilder()
                .emailAddress("first@example.com")
                .build();

        assertThrows(DuplicateEmailException.class,
                () -> service.updateCustomer(createdTwo.getId(), conflict));
    }

    @Test
    void shouldDeleteCustomerSuccessfully() {
        Customer customer = baseCustomer.toBuilder()
                .emailAddress("delete.me@example.com")
                .build();
        Customer created = service.createCustomer(customer);

        service.deleteCustomer(created.getId());
        assertTrue(service.getCustomerById(created.getId()).isEmpty());
    }

    @Test
    void shouldThrowWhenDeletingNonExistentCustomer() {
        assertThrows(CustomerNotFoundException.class,
                () -> service.deleteCustomer(UUID.randomUUID()));
    }

    @Test
    void getCustomerByEmail_shouldReturnCustomer() {
        Customer customer = baseCustomer.toBuilder()
                .emailAddress("lookup@example.com")
                .build();
        service.createCustomer(customer);

        assertTrue(service.getCustomerByEmail("lookup@example.com").isPresent());
    }

    @Test
    void existsById_shouldReflectPresenceCorrectly() {
        Customer customer = baseCustomer.toBuilder()
                .emailAddress("exists@example.com")
                .build();
        Customer created = service.createCustomer(customer);

        assertTrue(service.existsById(created.getId()));
        assertFalse(service.existsById(UUID.randomUUID()));
    }
}