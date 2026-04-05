package com.finance.dashboard.config;

import com.finance.dashboard.entity.Role;
import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.entity.TransactionType;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        log.info("Seeding initial data...");

        // Create default users
        User admin = User.builder()
                .username("admin")
                .email("admin@finance.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .active(true)
                .build();

        User analyst = User.builder()
                .username("analyst")
                .email("analyst@finance.com")
                .password(passwordEncoder.encode("analyst123"))
                .role(Role.ANALYST)
                .active(true)
                .build();

        User viewer = User.builder()
                .username("viewer")
                .email("viewer@finance.com")
                .password(passwordEncoder.encode("viewer123"))
                .role(Role.VIEWER)
                .active(true)
                .build();

        userRepository.save(admin);
        userRepository.save(analyst);
        userRepository.save(viewer);

        // Seed sample transactions
        String[][] samples = {
            {"5000.00", "INCOME",  "Salary",       "-90"},
            {"1200.00", "EXPENSE", "Rent",          "-85"},
            {"350.00",  "EXPENSE", "Groceries",     "-80"},
            {"800.00",  "INCOME",  "Freelance",     "-75"},
            {"200.00",  "EXPENSE", "Utilities",     "-70"},
            {"4800.00", "INCOME",  "Salary",        "-60"},
            {"1200.00", "EXPENSE", "Rent",          "-55"},
            {"420.00",  "EXPENSE", "Groceries",     "-50"},
            {"150.00",  "EXPENSE", "Entertainment", "-45"},
            {"5200.00", "INCOME",  "Salary",        "-30"},
            {"1200.00", "EXPENSE", "Rent",          "-25"},
            {"380.00",  "EXPENSE", "Groceries",     "-20"},
            {"300.00",  "INCOME",  "Bonus",         "-15"},
            {"180.00",  "EXPENSE", "Utilities",     "-10"},
            {"90.00",   "EXPENSE", "Entertainment", "-5"},
        };

        for (String[] s : samples) {
            transactionRepository.save(Transaction.builder()
                    .amount(new BigDecimal(s[0]))
                    .type(TransactionType.valueOf(s[1]))
                    .category(s[2])
                    .date(LocalDate.now().plusDays(Long.parseLong(s[3])))
                    .notes("Seeded transaction")
                    .createdBy(admin)
                    .build());
        }

        log.info("Seeding complete. Users: admin/admin123, analyst/analyst123, viewer/viewer123");
    }
}
