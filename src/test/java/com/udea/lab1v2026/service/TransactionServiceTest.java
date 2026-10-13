package com.udea.lab1v2026.service;

import com.udea.lab1v2026.DTO.TransactionDTO;
import com.udea.lab1v2026.entity.Customer;
import com.udea.lab1v2026.entity.Transaction;
import com.udea.lab1v2026.repository.CustomerRepository;
import com.udea.lab1v2026.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Customer customer(Long id, String account, double balance) {
        return new Customer(id, account, "Nombre", "Apellido", balance);
    }

    private TransactionDTO request(String sender, String receiver, Double amount) {
        TransactionDTO dto = new TransactionDTO();
        dto.setSenderAccountNumber(sender);
        dto.setReceiverAccountNumber(receiver);
        dto.setAmount(amount);
        return dto;
    }

    @Test
    void transferMoney_ok_updatesBalancesAndSavesTransaction() {
        Customer sender = customer(1L, "111", 1000.0);
        Customer receiver = customer(2L, "222", 500.0);
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("222")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        TransactionDTO result = transactionService.transferMoney(request("111", "222", 200.0));

        assertEquals(800.0, sender.getBalance());
        assertEquals(700.0, receiver.getBalance());
        assertEquals(10L, result.getId());
        assertEquals("111", result.getSenderAccountNumber());
        assertEquals("222", result.getReceiverAccountNumber());
        assertEquals(200.0, result.getAmount());
        assertNotNull(result.getTimestamp());
        verify(customerRepository, times(2)).save(any(Customer.class));
    }

    @Test
    void transferMoney_nullSender_throwsIllegalArgument() {
        TransactionDTO dto = request(null, "222", 10.0);

        assertThrows(IllegalArgumentException.class, () -> transactionService.transferMoney(dto));
    }

    @Test
    void transferMoney_nullReceiver_throwsIllegalArgument() {
        TransactionDTO dto = request("111", null, 10.0);

        assertThrows(IllegalArgumentException.class, () -> transactionService.transferMoney(dto));
    }

    @Test
    void transferMoney_nullAmount_throwsIllegalArgument() {
        TransactionDTO dto = request("111", "222", null);

        assertThrows(IllegalArgumentException.class, () -> transactionService.transferMoney(dto));
    }

    @Test
    void transferMoney_zeroOrNegativeAmount_throwsIllegalArgument() {
        TransactionDTO zero = request("111", "222", 0.0);
        TransactionDTO negative = request("111", "222", -5.0);

        assertThrows(IllegalArgumentException.class, () -> transactionService.transferMoney(zero));
        assertThrows(IllegalArgumentException.class, () -> transactionService.transferMoney(negative));
    }

    @Test
    void transferMoney_sameAccount_throwsIllegalArgument() {
        TransactionDTO dto = request("111", "111", 10.0);

        assertThrows(IllegalArgumentException.class, () -> transactionService.transferMoney(dto));
    }

    @Test
    void transferMoney_senderNotFound_throwsIllegalArgument() {
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.empty());
        TransactionDTO dto = request("111", "222", 10.0);

        assertThrows(IllegalArgumentException.class, () -> transactionService.transferMoney(dto));
    }

    @Test
    void transferMoney_receiverNotFound_throwsIllegalArgument() {
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.of(customer(1L, "111", 100.0)));
        when(customerRepository.findByAccountNumber("222")).thenReturn(Optional.empty());
        TransactionDTO dto = request("111", "222", 10.0);

        assertThrows(IllegalArgumentException.class, () -> transactionService.transferMoney(dto));
    }

    @Test
    void transferMoney_insufficientFunds_throwsAndDoesNotSave() {
        Customer sender = customer(1L, "111", 50.0);
        Customer receiver = customer(2L, "222", 0.0);
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("222")).thenReturn(Optional.of(receiver));
        TransactionDTO dto = request("111", "222", 100.0);

        assertThrows(IllegalArgumentException.class, () -> transactionService.transferMoney(dto));

        assertEquals(50.0, sender.getBalance());
        verify(customerRepository, never()).save(any(Customer.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void getTransactionsForAccount_returnsMappedDtos() {
        Transaction transaction = new Transaction();
        transaction.setId(5L);
        transaction.setSenderAccountNumber("111");
        transaction.setReceiverAccountNumber("222");
        transaction.setAmount(75.0);
        transaction.setTimestamp(LocalDateTime.of(2026, 10, 13, 10, 0));
        when(transactionRepository.findBySenderAccountNumberOrReceiverAccountNumber("111", "111"))
                .thenReturn(List.of(transaction));

        List<TransactionDTO> result = transactionService.getTransactionsForAccount("111");

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
        assertEquals("222", result.get(0).getReceiverAccountNumber());
        assertEquals(75.0, result.get(0).getAmount());
        assertEquals(LocalDateTime.of(2026, 10, 13, 10, 0), result.get(0).getTimestamp());
    }
}
