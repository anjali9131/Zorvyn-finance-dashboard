package com.finance.dashboard.service.impl;

import com.finance.dashboard.dto.request.TransactionRequest;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.entity.TransactionType;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TransactionResponse create(TransactionRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(user)
                .build();

        return toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAll(TransactionType type, String category,
                                            LocalDate startDate, LocalDate endDate,
                                            Pageable pageable) {
        return transactionRepository
                .findAllFiltered(type, category, startDate, endDate, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        Transaction transaction = findOrThrow(id);
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory().trim());
        transaction.setDate(request.getDate());
        transaction.setNotes(request.getNotes());
        return toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Transaction transaction = findOrThrow(id);
        transaction.setDeleted(true); // soft delete
        transactionRepository.save(transaction);
    }

    private Transaction findOrThrow(Long id) {
        return transactionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    public TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .category(t.getCategory())
                .date(t.getDate())
                .notes(t.getNotes())
                .createdBy(t.getCreatedBy().getUsername())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
