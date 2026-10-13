package com.udea.lab1v2026.service;

import com.udea.lab1v2026.DTO.TransactionDTO;
import com.udea.lab1v2026.entity.Customer;
import com.udea.lab1v2026.entity.Transaction;
import com.udea.lab1v2026.repository.CustomerRepository;
import com.udea.lab1v2026.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository; // Para validar cuentas

    public TransactionService(TransactionRepository transactionRepository, CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public TransactionDTO transferMoney(TransactionDTO transactionDTO) {
        // Validar que los numeros de cuenta no sean nulos
        if (transactionDTO.getSenderAccountNumber() == null || transactionDTO.getReceiverAccountNumber() == null) {
            throw new IllegalArgumentException("Los numeros de cuenta del remitente y receptor son obligatorios.");
        }

        // Validar el monto
        if (transactionDTO.getAmount() == null || transactionDTO.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }

        // No se puede transferir a la misma cuenta
        if (transactionDTO.getSenderAccountNumber().equals(transactionDTO.getReceiverAccountNumber())) {
            throw new IllegalArgumentException("La cuenta del remitente y la del receptor no pueden ser la misma.");
        }

        // Buscar los clientes por numero de cuenta
        Customer sender = customerRepository.findByAccountNumber(transactionDTO.getSenderAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta del remitente no existe."));
        Customer receiver = customerRepository.findByAccountNumber(transactionDTO.getReceiverAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta del receptor no existe."));

        // Validar que el remitente tenga saldo suficiente
        if (sender.getBalance() < transactionDTO.getAmount()) {
            throw new IllegalArgumentException("Saldo insuficiente en la cuenta del remitente.");
        }

        // Realizar la transferencia
        sender.setBalance(sender.getBalance() - transactionDTO.getAmount());
        receiver.setBalance(receiver.getBalance() + transactionDTO.getAmount());

        // Guardar los cambios en las cuentas
        customerRepository.save(sender);
        customerRepository.save(receiver);

        // Crear y guardar la transaccion
        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber(sender.getAccountNumber());
        transaction.setReceiverAccountNumber(receiver.getAccountNumber());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setTimestamp(LocalDateTime.now());

        return toDTO(transactionRepository.save(transaction));
    }

    public List<TransactionDTO> getTransactionsForAccount(String accountNumber) {
        return transactionRepository
                .findBySenderAccountNumberOrReceiverAccountNumber(accountNumber, accountNumber)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private TransactionDTO toDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setSenderAccountNumber(transaction.getSenderAccountNumber());
        dto.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
        dto.setAmount(transaction.getAmount());
        dto.setTimestamp(transaction.getTimestamp());
        return dto;
    }
}
