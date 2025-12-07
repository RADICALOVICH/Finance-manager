package vp.financemanager.core.service;

import vp.financemanager.core.models.Category;
import vp.financemanager.core.models.CategoryBudget;
import vp.financemanager.core.models.Wallet;
import vp.financemanager.core.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BudgetService {

    private final WalletRepository walletRepository;

    public BudgetService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public void setBudget(Wallet wallet, Category category, BigDecimal limit) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Limit cannot be null and must be greater than 0");
        }

        Map<Category, CategoryBudget> categoryBudgets = wallet.getCategoryBudgets();

        if (categoryBudgets.containsKey(category)) {
            CategoryBudget existing = categoryBudgets.get(category);
            existing.setLimit(limit);
        } else {
            CategoryBudget newCategoryBudget = new CategoryBudget(category, limit);
            categoryBudgets.put(category, newCategoryBudget);
        }
        walletRepository.save(wallet);
    }

    public CategoryBudget getBudget(Wallet wallet, Category category) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return wallet.getCategoryBudgets().get(category);
    }

    public List<CategoryBudget> getAllBudgets(Wallet wallet) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }

        return new ArrayList<>(wallet.getCategoryBudgets().values());
    }

    public BigDecimal getRemainingLimit(Wallet wallet, Category category) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        CategoryBudget budget = getBudget(wallet, category);
        if (budget == null) {
            // можно вернуть null или 0 — но лучше явно сказать, что лимит не задан
            throw new IllegalStateException("Budget is not set for category: " + category.getName());
        }

        return budget.getLimit().subtract(budget.getSpent());
    }

    public boolean isBudgetExceeded(Wallet wallet, Category category) {
        BigDecimal remaining = getRemainingLimit(wallet, category);
        return remaining.compareTo(BigDecimal.ZERO) < 0;
    }

}
