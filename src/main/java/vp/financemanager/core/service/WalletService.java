package vp.financemanager.core.service;

import vp.financemanager.core.models.*;
import vp.financemanager.core.repository.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class WalletService {
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
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

        wallet.getTransactions().add(income);
        wallet.setBalance(wallet.getBalance().add(amount));
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

        Transaction expense = new Transaction(
                TransactionType.EXPENSE,
                amount,
                category,
                description,
                LocalDateTime.now());

        wallet.getTransactions().add(expense);
        wallet.setBalance(wallet.getBalance().subtract(amount));


        CategoryBudget budget = wallet.getCategoryBudgets().get(category);
        if (budget != null) {
            budget.addSpent(amount);
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
}
