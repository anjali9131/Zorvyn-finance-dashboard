package com.finance.dashboard.repository;

import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndDeletedFalse(Long id);

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.deleted = false
          AND (:type IS NULL OR t.type = :type)
          AND (:category IS NULL OR LOWER(t.category) LIKE LOWER(CONCAT('%', :category, '%')))
          AND (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate IS NULL OR t.date <= :endDate)
    """)
    Page<Transaction> findAllFiltered(
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.deleted = false")
    BigDecimal sumByType(@Param("type") TransactionType type);

    @Query("""
        SELECT t.category, SUM(t.amount) FROM Transaction t
        WHERE t.type = :type AND t.deleted = false
        GROUP BY t.category
    """)
    List<Object[]> sumByCategory(@Param("type") TransactionType type);

    @Query("""
        SELECT FUNCTION('MONTH', t.date) as month, FUNCTION('YEAR', t.date) as year,
               t.type, SUM(t.amount)
        FROM Transaction t
        WHERE t.deleted = false
          AND t.date >= :startDate
        GROUP BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date), t.type
        ORDER BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date)
    """)
    List<Object[]> monthlyTrends(@Param("startDate") LocalDate startDate);

    @Query("SELECT t FROM Transaction t WHERE t.deleted = false ORDER BY t.createdAt DESC")
    List<Transaction> findRecentActivity(Pageable pageable);
}
