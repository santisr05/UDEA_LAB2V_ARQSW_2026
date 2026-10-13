package com.udea.lab1v2026.service;

import com.udea.lab1v2026.DTO.CustomerDTO;
import com.udea.lab1v2026.entity.Customer;
import com.udea.lab1v2026.exception.CustomerNotFoundException;
import com.udea.lab1v2026.mapper.CustomerMapper;
import com.udea.lab1v2026.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private CustomerDTO dto(double balance) {
        return new CustomerDTO(1L, "Ana", "Perez", "111", balance);
    }

    @Test
    void getAllCustomers_returnsMappedList() {
        Customer entity = new Customer();
        CustomerDTO dto = dto(100.0);
        when(customerRepository.findAll()).thenReturn(List.of(entity));
        when(customerMapper.toDTO(entity)).thenReturn(dto);

        List<CustomerDTO> result = customerService.getAllCustomers();

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void getCustomerById_found() {
        Customer entity = new Customer();
        CustomerDTO dto = dto(100.0);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(customerMapper.toDTO(entity)).thenReturn(dto);

        assertSame(dto, customerService.getCustomerById(1L));
    }

    @Test
    void getCustomerById_notFound_throwsCustomerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(99L));
    }

    @Test
    void createCustomer_savesAndReturnsDto() {
        CustomerDTO dto = dto(500.0);
        Customer entity = new Customer();
        when(customerMapper.toEntity(dto)).thenReturn(entity);
        when(customerRepository.save(entity)).thenReturn(entity);
        when(customerMapper.toDTO(entity)).thenReturn(dto);

        CustomerDTO result = customerService.createCustomer(dto);

        assertSame(dto, result);
        verify(customerRepository).save(entity);
    }

    @Test
    void createCustomer_nullBalance_throwsIllegalArgument() {
        CustomerDTO dto = new CustomerDTO(1L, "Ana", "Perez", "111", null);

        assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(dto));
        verifyNoInteractions(customerRepository);
    }

    @Test
    void createCustomer_negativeBalance_throwsIllegalArgument() {
        CustomerDTO dto = dto(-1.0);

        assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(dto));
        verifyNoInteractions(customerRepository);
    }
}
