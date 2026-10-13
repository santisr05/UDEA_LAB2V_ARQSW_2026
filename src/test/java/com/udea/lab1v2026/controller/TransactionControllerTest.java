package com.udea.lab1v2026.controller;

import com.udea.lab1v2026.DTO.TransactionDTO;
import com.udea.lab1v2026.service.TransactionService;
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
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private final TransactionDTO dto = new TransactionDTO(1L, "111", "222", 50.0, null);

    @Test
    void transferMoney_returns200WithTransaction() {
        when(transactionService.transferMoney(dto)).thenReturn(dto);

        ResponseEntity<TransactionDTO> response = transactionController.transferMoney(dto);

        assertEquals(200, response.getStatusCode().value());
        assertSame(dto, response.getBody());
    }

    @Test
    void getTransactionsByAccount_returns200WithList() {
        when(transactionService.getTransactionsForAccount("111")).thenReturn(List.of(dto));

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByAccount("111");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }
}
