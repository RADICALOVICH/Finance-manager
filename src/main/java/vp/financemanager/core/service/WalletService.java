package vp.financemanager.core.service;

import vp.financemanager.core.models.*;
import vp.financemanager.core.repository.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class WalletService {
    private final WalletRepository walletRepository;
    private final BudgetService budgetService;

    public WalletService(WalletRepository walletRepository, BudgetService budgetService) {
        this.walletRepository = walletRepository;
        this.budgetService = budgetService;
    }

    public void addIncome(Wallet wallet, BigDecimal amount, Category category, String description) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Income must be greater than 0");
        }

        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        Transaction income = new Transaction(
                TransactionType.INCOME,
                amount,
                category,
                description,
                LocalDateTime.now()
        );

        wallet.addTransaction(income);
        walletRepository.save(wallet);
    }

    public void addExpense(Wallet wallet, BigDecimal amount, Category category, String description){
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense must be greater than 0");
        }

        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        // Убеждаемся, что бюджет существует для категории расходов
        budgetService.ensureBudgetExists(wallet, category);
        
        // Находим правильную категорию (из budgets, если есть)
        Category existingCategory = category;
        CategoryBudget budget = wallet.getCategoryBudget(category);
        if (budget != null) {
            existingCategory = budget.getCategory();
        }
        
        // Создаем транзакцию с правильной категорией
        Transaction expense = new Transaction(
                TransactionType.EXPENSE,
                amount,
                existingCategory,
                description,
                LocalDateTime.now());
        
        wallet.addTransaction(expense);
        
        // Обновляем spent в бюджете
        CategoryBudget categoryBudget = wallet.getCategoryBudget(existingCategory);
        if (categoryBudget != null) {
            categoryBudget.addSpent(amount);
        }

        walletRepository.save(wallet);
    }

    public BigDecimal getTotalIncome(Wallet wallet) {
        return getTotalByTypeAndCategories(wallet, TransactionType.INCOME, null);
    }

    public BigDecimal getTotalExpense(Wallet wallet) {
        return getTotalByTypeAndCategories(wallet, TransactionType.EXPENSE, null);
    }

    public BigDecimal getTotalIncomeByCategory(Wallet wallet, Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return getTotalByTypeAndCategories(wallet, TransactionType.INCOME, List.of(category));
    }

    public BigDecimal getTotalExpenseByCategory(Wallet wallet, Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return getTotalByTypeAndCategories(wallet, TransactionType.EXPENSE, List.of(category));
    }


    public BigDecimal getTotalByCategories(Wallet wallet,
                                           List<Category> categories,
                                           TransactionType type) {
        return getTotalByTypeAndCategories(wallet, type, categories);
    }

    private BigDecimal getTotalByTypeAndCategories(Wallet wallet,
                                                   TransactionType type,
                                                   List<Category> categoriesOrNull) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }

        boolean filterByCategory = categoriesOrNull != null && !categoriesOrNull.isEmpty();

        BigDecimal total = BigDecimal.ZERO;

        for (Transaction tx : wallet.getTransactions()) {
            if (tx.getType() != type) {
                continue;
            }

            if (filterByCategory && !categoriesOrNull.contains(tx.getCategory())) {
                continue;
            }

            total = total.add(tx.getAmount());
        }

        return total;
    }

    public List<Transaction> getTransactions(Wallet wallet, 
                                             TransactionType type,
                                             List<Category> categories,
                                             LocalDate fromDate,
                                             LocalDate toDate) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }

        return wallet.getTransactions().stream()
                .filter(tx -> type == null || tx.getType() == type)
                .filter(tx -> categories == null || categories.isEmpty() || categories.contains(tx.getCategory()))
                .filter(tx -> {
                    if (fromDate == null && toDate == null) {
                        return true;
                    }
                    LocalDate txDate = tx.getTimestamp().toLocalDate();
                    boolean afterFrom = fromDate == null || !txDate.isBefore(fromDate);
                    boolean beforeTo = toDate == null || !txDate.isAfter(toDate);
                    return afterFrom && beforeTo;
                })
                .collect(Collectors.toList());
    }
}
