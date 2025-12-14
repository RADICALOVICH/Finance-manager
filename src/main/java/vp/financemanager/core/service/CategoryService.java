package vp.financemanager.core.service;

import vp.financemanager.core.models.Category;
import vp.financemanager.core.models.CategoryBudget;
import vp.financemanager.core.models.Transaction;
import vp.financemanager.core.models.TransactionType;
import vp.financemanager.core.models.Wallet;
import vp.financemanager.core.repository.WalletRepository;

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

    public void renameCategory(Wallet wallet, Category oldCategory, String newName, WalletRepository walletRepository) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if (oldCategory == null) {
            throw new IllegalArgumentException("Old category cannot be null");
        }
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("New category name cannot be null or blank");
        }
        
        String trimmedNewName = newName.trim();
        
        Category existingCategory = findCategoryByName(wallet, trimmedNewName);
        if (existingCategory != null && !existingCategory.equals(oldCategory)) {
            throw new IllegalArgumentException("Category with name '" + trimmedNewName + "' already exists");
        }
        
        Category newCategory = new Category(trimmedNewName);
        
        List<Transaction> transactions = wallet.getTransactions();
        for (int i = 0; i < transactions.size(); i++) {
            Transaction tx = transactions.get(i);
            if (tx.getCategory().equals(oldCategory)) {
                Transaction newTx = new Transaction(
                    tx.getType(),
                    tx.getAmount(),
                    newCategory,
                    tx.getDescription(),
                    tx.getTimestamp()
                );
                wallet.replaceTransaction(i, newTx);
            }
        }
        
        CategoryBudget budget = wallet.getCategoryBudget(oldCategory);
        if (budget != null) {
            CategoryBudget newBudget = new CategoryBudget(newCategory, budget.getLimit());
            newBudget.setSpent(budget.getSpent());
            wallet.replaceCategoryInBudget(oldCategory, newCategory, newBudget);
        }
        
        walletRepository.save(wallet);
    }
}