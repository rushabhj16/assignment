package com.assignment.backend.unitTests.service;

import com.assignment.backend.entity.Customer;
import com.assignment.backend.exception.CustomerNotFoundException;
import com.assignment.backend.exception.DuplicateEmailException;
import com.assignment.backend.repository.CustomerRepository;

import com.assignment.backend.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private CustomerServiceImpl service;

    private final UUID customerId = UUID.randomUUID();
    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleCustomer = Customer.builder()
                .id(customerId)
                .givenName("John")
                .middleName("M")
                .familyName("Doe")
                .emailAddress("john@example.com")
                .contactNumber("+1234567890")
                .build();
    }

    @Test
    void getAllCustomers_shouldReturnList() {
        when(repository.findAll()).thenReturn(List.of(sampleCustomer));
        List<Customer> customers = service.getAllCustomers();

        assertEquals(1, customers.size());
        assertEquals("John", customers.get(0).getGivenName());
    }

    @Test
    void getCustomerById_found() {
        when(repository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));

        Optional<Customer> found = service.getCustomerById(customerId);
        assertTrue(found.isPresent());
        assertEquals("john@example.com", found.get().getEmailAddress());
    }

    @Test
    void getCustomerById_notFound() {
        when(repository.findById(customerId)).thenReturn(Optional.empty());

        Optional<Customer> found = service.getCustomerById(customerId);
        assertTrue(found.isEmpty());
    }

    @Test
    void getCustomerByEmail_shouldReturnCustomer() {
        when(repository.findByEmailAddress("john@example.com")).thenReturn(Optional.of(sampleCustomer));

        Optional<Customer> result = service.getCustomerByEmail("john@example.com");
        assertTrue(result.isPresent());
        assertEquals("Doe", result.get().getFamilyName());
    }

    @Test
    void createCustomer_success() {
        when(repository.existsByEmailAddress("john@example.com")).thenReturn(false);
        when(repository.save(any(Customer.class))).thenReturn(sampleCustomer);

        Customer created = service.createCustomer(sampleCustomer);
        assertNotNull(created);
        assertEquals("john@example.com", created.getEmailAddress());
        verify(repository).save(any(Customer.class));
    }

    @Test
    void createCustomer_duplicateEmail_shouldThrow() {
        when(repository.existsByEmailAddress("john@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> service.createCustomer(sampleCustomer));
        verify(repository, never()).save(any());
    }

    @Test
    void updateCustomer_success() {
        Customer updated = Customer.builder()
                .givenName("Updated")
                .emailAddress("john@example.com")
                .build();

        when(repository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));
        when(repository.save(any(Customer.class))).thenReturn(updated);

        Customer result = service.updateCustomer(customerId, updated);

        assertEquals("Updated", result.getGivenName());
    }

    @Test
    void updateCustomer_duplicateEmail_shouldThrow() {
        Customer updated = Customer.builder()
                .givenName("Updated")
                .emailAddress("different@example.com")
                .build();

        when(repository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));
        when(repository.existsByEmailAddress("different@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> service.updateCustomer(customerId, updated));
        verify(repository, never()).save(any());
    }

    @Test
    void updateCustomer_notFound_shouldThrow() {
        when(repository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> service.updateCustomer(customerId, sampleCustomer));
    }

    @Test
    void deleteCustomer_success() {
        when(repository.existsById(customerId)).thenReturn(true);
        doNothing().when(repository).deleteById(customerId);

        assertDoesNotThrow(() -> service.deleteCustomer(customerId));
        verify(repository).deleteById(customerId);
    }

    @Test
    void deleteCustomer_notFound_shouldThrow() {
        when(repository.existsById(customerId)).thenReturn(false);

        assertThrows(CustomerNotFoundException.class, () -> service.deleteCustomer(customerId));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void existsById_shouldReturnTrue() {
        when(repository.existsById(customerId)).thenReturn(true);
        assertTrue(service.existsById(customerId));
    }

    @Test
    void createCustomer_shouldNormalizeEmail() {
        Customer input = Customer.builder()
                .id(sampleCustomer.getId())
                .givenName(sampleCustomer.getGivenName())
                .middleName(sampleCustomer.getMiddleName())
                .familyName(sampleCustomer.getFamilyName())
                .contactNumber(sampleCustomer.getContactNumber())
                .emailAddress("  JoHN@EXAMPLE.Com ")
                .build();

        when(repository.existsByEmailAddress("john@example.com")).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Customer created = service.createCustomer(input);

        assertEquals("john@example.com", created.getEmailAddress());
    }

    @Test
    void updateCustomer_sameEmail_shouldNotCheckDuplicate() {
        Customer updated = Customer.builder()
                .id(sampleCustomer.getId())
                .givenName("Jane")
                .middleName(sampleCustomer.getMiddleName())
                .familyName(sampleCustomer.getFamilyName())
                .emailAddress("john@example.com")  // same email
                .contactNumber(sampleCustomer.getContactNumber())
                .build();

        when(repository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));
        when(repository.save(any())).thenReturn(updated);

        Customer result = service.updateCustomer(customerId, updated);

        assertEquals("Jane", result.getGivenName());
        assertEquals("john@example.com", result.getEmailAddress());
    }

    @Test
    void updateCustomer_shouldTrimAndLowercaseEmail() {
        Customer updated = Customer.builder()
                .id(sampleCustomer.getId())
                .givenName(sampleCustomer.getGivenName())
                .middleName(sampleCustomer.getMiddleName())
                .familyName(sampleCustomer.getFamilyName())
                .contactNumber(sampleCustomer.getContactNumber())
                .emailAddress("  JOHN@Example.com ")
                .build();

        when(repository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = service.updateCustomer(customerId, updated);

        assertEquals("john@example.com", result.getEmailAddress());
    }

    @Test
    void existsById_shouldReturnFalse() {
        when(repository.existsById(customerId)).thenReturn(false);
        assertFalse(service.existsById(customerId));
    }
}
