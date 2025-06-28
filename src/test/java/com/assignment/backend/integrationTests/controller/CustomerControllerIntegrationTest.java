package com.assignment.backend.integrationTests.controller;

import com.assignment.backend.dto.CustomerRequestDTO;
import com.assignment.backend.entity.Customer;
import com.assignment.backend.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("acceptance")
@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CustomerRepository repository;

    private Customer testCustomer;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        testCustomer = repository.save(Customer.builder()
                .givenName("Clark")
                .middleName("J")
                .familyName("Kent")
                .emailAddress("clark.integration@example.com")
                .contactNumber("+1234567899")
                .build());
    }

    @Test
    void getAllCustomers_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk());
    }

    @Test
    void getCustomerById_found() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", testCustomer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailAddress").value("clark.integration@example.com"));
    }

    @Test
    void getCustomerById_notFound() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void headRequest_shouldReturnCorrectStatus() throws Exception {
        mockMvc.perform(head("/api/customers/{id}", testCustomer.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void searchByEmail_found() throws Exception {
        mockMvc.perform(get("/api/customers/search")
                        .param("email", "clark.integration@example.com"))
                .andExpect(status().isOk());
    }

    @Test
    void searchByEmail_notFound() throws Exception {
        mockMvc.perform(get("/api/customers/search")
                        .param("email", "unknown@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCustomer_success() throws Exception {
        CustomerRequestDTO dto = CustomerRequestDTO.builder()
                .givenName("Bruce")
                .middleName("T")
                .familyName("Wayne")
                .emailAddress("bruce@example.com")
                .contactNumber("+1987654321")
                .build();

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void createCustomer_invalidEmail() throws Exception {
        CustomerRequestDTO dto = CustomerRequestDTO.builder()
                .givenName("John")
                .familyName("Doe")
                .emailAddress("bad-email")
                .contactNumber("+1234567890")
                .build();

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCustomer_success() throws Exception {
        CustomerRequestDTO dto = CustomerRequestDTO.builder()
                .givenName("Clark")
                .middleName("Updated")
                .familyName("Kent")
                .emailAddress("clark.integration@example.com")
                .contactNumber("+1111111111")
                .build();

        mockMvc.perform(put("/api/customers/{id}", testCustomer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void patchContactNumber_success() throws Exception {
        mockMvc.perform(patch("/api/customers/{id}/contact", testCustomer.getId())
                        .param("contactNumber", "+10987654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactNumber").value("+10987654321"));
    }

    @Test
    void deleteCustomer_success() throws Exception {
        mockMvc.perform(delete("/api/customers/{id}", testCustomer.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void options_shouldListMethods() throws Exception {
        mockMvc.perform(options("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(header().string("Allow", containsString("GET")))
                .andExpect(header().string("Allow", containsString("POST")))
                .andExpect(header().string("Allow", containsString("OPTIONS")));
    }
}