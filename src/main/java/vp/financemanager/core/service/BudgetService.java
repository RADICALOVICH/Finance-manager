package vp.financemanager.core.service;

import vp.financemanager.core.models.Category;
import vp.financemanager.core.models.CategoryBudget;
import vp.financemanager.core.models.Transaction;
import vp.financemanager.core.models.TransactionType;
import vp.financemanager.core.models.Wallet;
import vp.financemanager.core.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BudgetService {

    private final WalletRepository walletRepository;
    private final CategoryService categoryService;

    public BudgetService(WalletRepository walletRepository, CategoryService categoryService) {
        this.walletRepository = walletRepository;
        this.categoryService = categoryService;
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

        // Проверяем, используется ли категория только для доходов
        if (isCategoryOnlyForIncome(wallet, category)) {
            throw new IllegalArgumentException("Cannot set budget for category '" + category.getName() 
                    + "' - it is used only for income transactions. Budgets can only be set for expense categories.");
        }

        // Найти существующую категорию в бюджетах (case-insensitive) или использовать переданную
        Category existingCategory = categoryService.findCategoryInBudgets(wallet, category);
        if (existingCategory == null) {
            existingCategory = category;
        }

        CategoryBudget budget = wallet.getCategoryBudget(existingCategory);
        if (budget != null) {
            budget.setLimit(limit);
        } else {
            budget = new CategoryBudget(existingCategory, limit);
            wallet.addCategoryBudget(existingCategory, budget);
        }
        
        // Пересчитать spent на основе всех существующих транзакций
        recalculateSpent(wallet, existingCategory, budget);
        
        walletRepository.save(wallet);
    }
    
    private boolean isCategoryOnlyForIncome(Wallet wallet, Category category) {
        boolean hasIncome = false;
        boolean hasExpense = false;
        
        String categoryName = category.getName();
        for (Transaction tx : wallet.getTransactions()) {
            if (tx.getCategory().getName().equalsIgnoreCase(categoryName)) {
                if (tx.getType() == TransactionType.INCOME) {
                    hasIncome = true;
                } else if (tx.getType() == TransactionType.EXPENSE) {
                    hasExpense = true;
                }
            }
        }
        
        return hasIncome && !hasExpense;
    }
    
    private void recalculateSpent(Wallet wallet, Category category, CategoryBudget budget) {
        BigDecimal totalSpent = BigDecimal.ZERO;
        
        for (Transaction tx : wallet.getTransactions()) {
            if (tx.getType() == TransactionType.EXPENSE 
                    && tx.getCategory().getName().equalsIgnoreCase(category.getName())) {
                totalSpent = totalSpent.add(tx.getAmount());
            }
        }
        
        budget.setSpent(totalSpent);
    }

    public CategoryBudget getBudget(Wallet wallet, Category category) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return wallet.getCategoryBudget(category);
    }

    public List<CategoryBudget> getAllBudgets(Wallet wallet) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }

        // Фильтруем категории, которые используются только для доходов
        List<CategoryBudget> expenseBudgets = new ArrayList<>();
        for (Category category : wallet.getCategoryBudgets().keySet()) {
            CategoryBudget budget = wallet.getCategoryBudget(category);
            if (budget != null && !isCategoryOnlyForIncome(wallet, category)) {
                expenseBudgets.add(budget);
            }
        }
        
        return expenseBudgets;
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
            throw new IllegalStateException("Budget is not set for category: " + category.getName());
        }

        return budget.getLimit().subtract(budget.getSpent());
    }

    public boolean isBudgetExceeded(Wallet wallet, Category category) {
        CategoryBudget budget = wallet.getCategoryBudget(category);
        if (budget == null) {
            return false;
        }
        BigDecimal remaining = budget.getLimit().subtract(budget.getSpent());
        return remaining.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isBudgetNearLimit(Wallet wallet, Category category) {
        CategoryBudget budget = wallet.getCategoryBudget(category);
        if (budget == null || budget.getLimit().compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        
        BigDecimal spent = budget.getSpent();
        BigDecimal limit = budget.getLimit();
        BigDecimal eightyPercent = limit.multiply(new BigDecimal("0.8"));
        
        return spent.compareTo(eightyPercent) >= 0 && !isBudgetExceeded(wallet, category);
    }

    public void ensureBudgetExists(Wallet wallet, Category category) {
        if (wallet == null || category == null) {
            return;
        }
        
        // Найти существующую категорию в бюджетах
        Category existingCategory = categoryService.findCategoryInBudgets(wallet, category);
        if (existingCategory == null) {
            existingCategory = category;
        }
        
        // Создать бюджет с лимитом 0, если его нет
        if (!wallet.hasCategoryBudget(existingCategory)) {
            CategoryBudget newBudget = new CategoryBudget(existingCategory, BigDecimal.ZERO);
            wallet.addCategoryBudget(existingCategory, newBudget);
            walletRepository.save(wallet);
        }
    }

}
