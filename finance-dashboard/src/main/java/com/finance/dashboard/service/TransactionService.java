package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.TransactionRequest;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface TransactionService {
    TransactionResponse create(TransactionRequest request, String username);
    Page<TransactionResponse> getAll(TransactionType type, String category,
                                     LocalDate startDate, LocalDate endDate, Pageable pageable);
    TransactionResponse getById(Long id);
    TransactionResponse update(Long id, TransactionRequest request);
    void delete(Long id);
}
