package com.udea.lab1v2026.controller;

import com.udea.lab1v2026.DTO.TransactionDTO;
import com.udea.lab1v2026.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/transactions", produces = "application/json")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Los errores (400, 404, 409) los traduce GlobalExceptionHandler
    @PostMapping
    public ResponseEntity<TransactionDTO> transferMoney(@RequestBody TransactionDTO transactionDTO) {
        return ResponseEntity.ok(transactionService.transferMoney(transactionDTO));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.getTransactionsForAccount(accountNumber));
    }
}
