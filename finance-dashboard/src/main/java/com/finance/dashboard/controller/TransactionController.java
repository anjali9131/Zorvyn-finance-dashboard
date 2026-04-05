package com.finance.dashboard.controller;

import com.finance.dashboard.dto.request.TransactionRequest;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.entity.TransactionType;
import com.finance.dashboard.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * POST /api/transactions
     * Roles: ANALYST, ADMIN
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> create(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TransactionResponse response = transactionService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created successfully.", response));
    }

    /**
     * GET /api/transactions?type=&category=&startDate=&endDate=&page=0&size=10&sort=date,desc
     * Roles: VIEWER, ANALYST, ADMIN
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAll(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date,desc") String[] sort) {

        if (size > 100) size = 100; // cap page size
        Sort sorting = buildSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<TransactionResponse> result = transactionService.getAll(type, category, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/transactions/{id}
     * Roles: VIEWER, ANALYST, ADMIN
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(transactionService.getById(id)));
    }

    /**
     * PUT /api/transactions/{id}
     * Roles: ANALYST, ADMIN
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Transaction updated successfully.", transactionService.update(id, request)));
    }

    /**
     * DELETE /api/transactions/{id} — soft delete
     * Roles: ADMIN
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully.", null));
    }

    private Sort buildSort(String[] sort) {
        if (sort.length == 2) {
            Sort.Direction dir = sort[1].equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            return Sort.by(dir, sort[0]);
        }
        return Sort.by(Sort.Direction.DESC, "date");
    }
}
