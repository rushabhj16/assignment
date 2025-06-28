package com.assignment.backend.unitTests.controller;

import com.assignment.backend.controller.CustomerController;
import com.assignment.backend.dto.CustomerRequestDTO;
import com.assignment.backend.entity.Customer;
import com.assignment.backend.exception.CustomerNotFoundException;
import com.assignment.backend.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    private final UUID testId = UUID.randomUUID();
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CustomerService service;
    @Autowired
    private ObjectMapper objectMapper;

    private Customer testCustomer() {
        return Customer.builder()
                .id(testId)
                .givenName("Alice")
                .middleName("M")
                .familyName("Smith")
                .emailAddress("alice@example.com")
                .contactNumber("+1234567890")
                .build();
    }

    @Test
    void getAllCustomers_shouldReturnList() throws Exception {
        Mockito.when(service.getAllCustomers()).thenReturn(List.of(testCustomer()));

        mockMvc.perform(get("/api/v1.0/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].givenName").value("Alice"));
    }

    @Test
    void getCustomerById_found() throws Exception {
        Mockito.when(service.getCustomerById(testId)).thenReturn(Optional.of(testCustomer()));

        mockMvc.perform(get("/api/v1.0/customers/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailAddress").value("alice@example.com"));
    }

    @Test
    void getCustomerById_notFound() throws Exception {
        Mockito.when(service.getCustomerById(testId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1.0/customers/{id}", testId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCustomerByEmail_found() throws Exception {
        Mockito.when(service.getCustomerByEmail("alice@example.com")).thenReturn(Optional.of(testCustomer()));

        mockMvc.perform(get("/api/v1.0/customers/search").param("email", "alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.familyName").value("Smith"));
    }

    @Test
    void createCustomer_validRequest() throws Exception {
        CustomerRequestDTO dto = CustomerRequestDTO.builder()
                .givenName("Alice")
                .middleName("M")
                .familyName("Smith")
                .emailAddress("alice@example.com")
                .contactNumber("+1234567890")
                .build();

        Mockito.when(service.createCustomer(any(Customer.class))).thenReturn(testCustomer());

        mockMvc.perform(post("/api/v1.0/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.givenName").value("Alice"));
    }

    @Test
    void createCustomer_invalidEmail_shouldFail() throws Exception {
        CustomerRequestDTO dto = CustomerRequestDTO.builder()
                .givenName("Alice")
                .middleName("M")
                .familyName("Smith")
                .emailAddress("invalid-email")
                .contactNumber("+1234567890")
                .build();

        mockMvc.perform(post("/api/v1.0/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCustomer_shouldReturnOk() throws Exception {
        CustomerRequestDTO dto = CustomerRequestDTO.builder()
                .givenName("Alice")
                .middleName("M")
                .familyName("Smith")
                .emailAddress("alice@example.com")
                .contactNumber("+1234567890")
                .build();

        Mockito.when(service.updateCustomer(eq(testId), any(Customer.class))).thenReturn(testCustomer());

        mockMvc.perform(put("/api/v1.0/customers/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailAddress").value("alice@example.com"));
    }

    @Test
    void deleteCustomer_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1.0/customers/{id}", testId))
                .andExpect(status().isNoContent());
    }

    @Test
    void patchContactNumber_valid() throws Exception {
        Mockito.when(service.getCustomerById(testId)).thenReturn(Optional.of(testCustomer()));
        Mockito.when(service.updateCustomer(eq(testId), any(Customer.class))).thenReturn(testCustomer());

        mockMvc.perform(patch("/api/v1.0/customers/{id}/contact", testId)
                        .param("contactNumber", "+19876543210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.givenName").value("Alice"));
    }

    @Test
    void headRequest_shouldReturnOkOrNotFound() throws Exception {
        Mockito.when(service.getCustomerById(testId)).thenReturn(Optional.of(testCustomer()));

        mockMvc.perform(head("/api/v1.0/customers/{id}", testId))
                .andExpect(status().isOk());
    }

    @Test
    void optionsRequest_shouldListAllowedMethods() throws Exception {
        mockMvc.perform(options("/api/v1.0/customers"))
                .andExpect(status().isOk())
                .andExpect(header().string("Allow", "GET,POST,PUT,PATCH,DELETE,OPTIONS,HEAD"));
    }

    @Test
    void patchContactNumber_invalidFormat_shouldFailValidation() throws Exception {
        mockMvc.perform(patch("/api/v1.0/customers/{id}/contact", testId)
                        .param("contactNumber", "123")) // too short, invalid format
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCustomer_missingEmail_shouldFailValidation() throws Exception {
        CustomerRequestDTO dto = CustomerRequestDTO.builder()
                .givenName("Alice")
                .middleName("M")
                .familyName("Smith")
                .contactNumber("+1234567890")
                .build(); // emailAddress missing

        mockMvc.perform(post("/api/v1.0/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCustomer_notFound_shouldReturnNotFound() throws Exception {
        doThrow(new CustomerNotFoundException("Customer not found"))
                .when(service).deleteCustomer(testId);

        mockMvc.perform(delete("/api/v1.0/customers/{id}", testId))
                .andExpect(status().isNotFound());
    }
}