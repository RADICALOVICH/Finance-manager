package vp.financemanager.core.service;

import vp.financemanager.core.models.Category;
import vp.financemanager.core.models.CategoryBudget;
import vp.financemanager.core.models.Transaction;
import vp.financemanager.core.models.Wallet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryService {

    public CategoryService() {
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

        // Создаем новую категорию (НЕ создаем CategoryBudget автоматически)
        // CategoryBudget будет создан только при добавлении расходов или установке бюджета
        Category newCategory = new Category(trimmedName);
        
        return newCategory;
    }

    public Category findCategoryByName(Wallet wallet, String name) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be null or blank");
        }

        String trimmedName = name.trim();
        
        // Сначала ищем в categoryBudgets
        Map<Category, CategoryBudget> budgets = wallet.getCategoryBudgets();
        for (Category category : budgets.keySet()) {
            if (category.getName().equalsIgnoreCase(trimmedName)) {
                return category;
            }
        }
        
        // Если не найдено в budgets, ищем в транзакциях
        for (Transaction tx : wallet.getTransactions()) {
            Category category = tx.getCategory();
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

    /**
     * Находит категорию в бюджетах кошелька (case-insensitive)
     * @return существующую категорию из budgets или null, если не найдена
     */
    public Category findCategoryInBudgets(Wallet wallet, Category category) {
        if (wallet == null || category == null) {
            return null;
        }
        
        // Сначала проверяем точное совпадение
        if (wallet.hasCategoryBudget(category)) {
            return category;
        }
        
        // Ищем по имени (case-insensitive)
        String categoryName = category.getName();
        for (Category existingCategory : wallet.getCategoryBudgets().keySet()) {
            if (existingCategory.getName().equalsIgnoreCase(categoryName)) {
                return existingCategory;
            }
        }
        
        return null;
    }
}