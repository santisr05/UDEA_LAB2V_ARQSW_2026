package com.udea.lab1v2026.mapper;

import com.udea.lab1v2026.DTO.CustomerDTO;
import com.udea.lab1v2026.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);
    CustomerDTO toDTO(Customer customer);
    Customer toEntity(CustomerDTO customerDTO);

}
