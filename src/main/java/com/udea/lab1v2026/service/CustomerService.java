package com.udea.lab1v2026.service;

import com.udea.lab1v2026.DTO.CustomerDTO;
import com.udea.lab1v2026.entity.Customer;
import com.udea.lab1v2026.exception.CustomerNotFoundException;
import com.udea.lab1v2026.mapper.CustomerMapper;
import com.udea.lab1v2026.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toDTO).toList();
    }

    public CustomerDTO getCustomerById(Long id) {
        return customerRepository.findById(id).map(customerMapper::toDTO)
                .orElseThrow(() -> new CustomerNotFoundException("Cliente no encontrado"));
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        if (customerDTO.getBalance() == null) {
            throw new IllegalArgumentException("El saldo (balance) es obligatorio.");
        }
        if (customerDTO.getBalance() < 0) {
            throw new IllegalArgumentException("El saldo (balance) no puede ser negativo.");
        }
        Customer customer = customerMapper.toEntity(customerDTO);
        return customerMapper.toDTO(customerRepository.save(customer));
    }
}
