package com.finance.dashboard.service.impl;

import com.finance.dashboard.dto.response.DashboardSummary;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.entity.TransactionType;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;
    private final TransactionServiceImpl transactionService;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummary getSummary() {
        BigDecimal totalIncome   = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);

        Map<String, BigDecimal> incomeByCategory  = buildCategoryMap(TransactionType.INCOME);
        Map<String, BigDecimal> expensesByCategory = buildCategoryMap(TransactionType.EXPENSE);

        List<TransactionResponse> recentActivity = transactionRepository
                .findRecentActivity(PageRequest.of(0, 10))
                .stream()
                .map(transactionService::toResponse)
                .toList();

        List<DashboardSummary.MonthlyTrend> monthlyTrends = buildMonthlyTrends();

        return DashboardSummary.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .incomeByCategory(incomeByCategory)
                .expensesByCategory(expensesByCategory)
                .recentActivity(recentActivity)
                .monthlyTrends(monthlyTrends)
                .build();
    }

    private Map<String, BigDecimal> buildCategoryMap(TransactionType type) {
        List<Object[]> rows = transactionRepository.sumByCategory(type);
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        rows.forEach(row -> result.put((String) row[0], (BigDecimal) row[1]));
        return result;
    }

    private List<DashboardSummary.MonthlyTrend> buildMonthlyTrends() {
        LocalDate startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        List<Transaction> txns = transactionRepository.findForTrends(startDate);

        Map<String, BigDecimal[]> trendMap = new LinkedHashMap<>();
        for (Transaction t : txns) {
            int year  = t.getDate().getYear();
            int month = t.getDate().getMonthValue();
            String key = year + "-" + String.format("%02d", month);
            trendMap.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (t.getType() == TransactionType.INCOME) {
                trendMap.get(key)[0] = trendMap.get(key)[0].add(t.getAmount());
            } else {
                trendMap.get(key)[1] = trendMap.get(key)[1].add(t.getAmount());
            }
        }

        return trendMap.entrySet().stream().map(entry -> {
            String[] parts = entry.getKey().split("-");
            int year  = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            BigDecimal income   = entry.getValue()[0];
            BigDecimal expenses = entry.getValue()[1];
            return DashboardSummary.MonthlyTrend.builder()
                    .year(year).month(month)
                    .monthName(Month.of(month).name())
                    .income(income).expenses(expenses)
                    .net(income.subtract(expenses))
                    .build();
        }).collect(Collectors.toList());
    }
}
