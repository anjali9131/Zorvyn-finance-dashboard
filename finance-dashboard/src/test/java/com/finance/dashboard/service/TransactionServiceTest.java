package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.TransactionRequest;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.entity.Role;
import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.entity.TransactionType;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock UserRepository userRepository;
    @InjectMocks TransactionServiceImpl transactionService;

    private User adminUser;
    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L).username("admin").email("admin@test.com")
                .password("enc").role(Role.ADMIN).active(true).build();

        sampleTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("500.00"))
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(LocalDate.now())
                .notes("Test")
                .createdBy(adminUser)
                .deleted(false)
                .build();
    }

    @Test
    void create_validRequest_returnsTransactionResponse() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("500.00"));
        req.setType(TransactionType.INCOME);
        req.setCategory("Salary");
        req.setDate(LocalDate.now());

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(transactionRepository.save(any())).thenReturn(sampleTransaction);

        TransactionResponse result = transactionService.create(req, "admin");

        assertThat(result.getAmount()).isEqualByComparingTo("500.00");
        assertThat(result.getCategory()).isEqualTo("Salary");
        assertThat(result.getCreatedBy()).isEqualTo("admin");
    }

    @Test
    void getById_existingId_returnsResponse() {
        when(transactionRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(sampleTransaction));

        TransactionResponse result = transactionService.getById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_deletedOrMissing_throwsNotFound() {
        when(transactionRepository.findByIdAndDeletedFalse(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_marksAsDeleted() {
        when(transactionRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(sampleTransaction));
        when(transactionRepository.save(any())).thenReturn(sampleTransaction);

        transactionService.delete(1L);

        assertThat(sampleTransaction.isDeleted()).isTrue();
        verify(transactionRepository).save(sampleTransaction);
    }

    @Test
    void update_changesFields() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("999.00"));
        req.setType(TransactionType.EXPENSE);
        req.setCategory("Updated");
        req.setDate(LocalDate.now());

        when(transactionRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(sampleTransaction));
        when(transactionRepository.save(any())).thenReturn(sampleTransaction);

        transactionService.update(1L, req);

        assertThat(sampleTransaction.getAmount()).isEqualByComparingTo("999.00");
        assertThat(sampleTransaction.getCategory()).isEqualTo("Updated");
        assertThat(sampleTransaction.getType()).isEqualTo(TransactionType.EXPENSE);
    }
}
