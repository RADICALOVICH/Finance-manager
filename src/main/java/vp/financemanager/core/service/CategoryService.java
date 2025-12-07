package vp.financemanager.core.service;

import vp.financemanager.core.models.Category;
import vp.financemanager.core.models.CategoryBudget;
import vp.financemanager.core.models.Wallet;
import vp.financemanager.core.repository.WalletRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryService {

    private final WalletRepository walletRepository;

    public CategoryService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Category createCategory(Wallet wallet, String name) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be null or blank");
        }

        String trimmedName = name.trim();

        Category existing = findCategoryByName(wallet, trimmedName);
        if (existing != null) {
            return existing;
        }

        return new Category(trimmedName);
    }

    public Category findCategoryByName(Wallet wallet, String name) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be null or blank");
        }

        String trimmedName = name.trim();
        Map<Category, CategoryBudget> budgets = wallet.getCategoryBudgets();

        for (Category category : budgets.keySet()) {
            if (category.getName().equalsIgnoreCase(trimmedName)) {
                return category;
            }
        }

        return null;
    }

    public List<Category> getAllCategories(Wallet wallet) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }

        return new ArrayList<>(wallet.getCategoryBudgets().keySet());
    }
}