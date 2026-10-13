package com.udea.lab1v2026.controller;

import com.udea.lab1v2026.DTO.CustomerDTO;
import com.udea.lab1v2026.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private final CustomerDTO dto = new CustomerDTO(1L, "Ana", "Perez", "111", 100.0);

    @Test
    void getAllCustomers_returns200WithList() {
        when(customerService.getAllCustomers()).thenReturn(List.of(dto));

        ResponseEntity<List<CustomerDTO>> response = customerController.getAllCustomers();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getCustomerById_returns200WithCustomer() {
        when(customerService.getCustomerById(1L)).thenReturn(dto);

        ResponseEntity<CustomerDTO> response = customerController.getCustomerById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertSame(dto, response.getBody());
    }

    @Test
    void createCustomer_returns200WithCreatedCustomer() {
        when(customerService.createCustomer(dto)).thenReturn(dto);

        ResponseEntity<CustomerDTO> response = customerController.createCustomer(dto);

        assertEquals(200, response.getStatusCode().value());
        assertSame(dto, response.getBody());
    }
}
