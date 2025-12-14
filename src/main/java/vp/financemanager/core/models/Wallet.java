package vp.financemanager.core.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wallet {
    private final String ownerLogin;
    // текущий баланс кошелька
    private BigDecimal balance;

    // доходы и расходы по кошельку
    private final List<Transaction> transactions;

    // бюджеты по категориям: категория и объект бюджета
    private final Map<Category, CategoryBudget> categoryBudgets;

    public Wallet(String ownerLogin, BigDecimal initialBalance) {
        if (ownerLogin == null || ownerLogin.isBlank()) {
            throw new IllegalArgumentException("Owner login cannot be null or blank");
        }
        if (initialBalance == null) {
            throw new IllegalArgumentException("Initial balance cannot be null");
        }
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        this.ownerLogin = ownerLogin.trim();
        this.balance = initialBalance;
        this.transactions = new ArrayList<>();
        this.categoryBudgets = new HashMap<>();
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public void setBalance(BigDecimal balance) {
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        this.balance = balance;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public Map<Category, CategoryBudget> getCategoryBudgets() {
        return Collections.unmodifiableMap(categoryBudgets);
    }

    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        this.transactions.add(transaction);
        
        if (transaction.getType() == TransactionType.INCOME) {
            this.balance = this.balance.add(transaction.getAmount());
        } else if (transaction.getType() == TransactionType.EXPENSE) {
            this.balance = this.balance.subtract(transaction.getAmount());
        }
    }

    public void addCategoryBudget(Category category, CategoryBudget budget) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (budget == null) {
            throw new IllegalArgumentException("Budget cannot be null");
        }
        this.categoryBudgets.put(category, budget);
    }

    public CategoryBudget getCategoryBudget(Category category) {
        if (category == null) {
            return null;
        }
        return this.categoryBudgets.get(category);
    }

    public boolean hasCategoryBudget(Category category) {
        if (category == null) {
            return false;
        }
        return this.categoryBudgets.containsKey(category);
    }

    public void restoreTransaction(Transaction transaction) {
        if (transaction == null) {
            return;
        }
        this.transactions.add(transaction);
    }

    public void restoreBalance(BigDecimal balance) {
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        this.balance = balance;
    }

    public void replaceTransaction(int index, Transaction newTransaction) {
        if (index < 0 || index >= transactions.size()) {
            throw new IllegalArgumentException("Invalid transaction index");
        }
        if (newTransaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        
        Transaction oldTransaction = transactions.get(index);
        
        if (oldTransaction.getType() == TransactionType.INCOME) {
            this.balance = this.balance.subtract(oldTransaction.getAmount());
        } else if (oldTransaction.getType() == TransactionType.EXPENSE) {
            this.balance = this.balance.add(oldTransaction.getAmount());
        }
        
        transactions.set(index, newTransaction);
        
        if (newTransaction.getType() == TransactionType.INCOME) {
            this.balance = this.balance.add(newTransaction.getAmount());
        } else if (newTransaction.getType() == TransactionType.EXPENSE) {
            this.balance = this.balance.subtract(newTransaction.getAmount());
        }
    }

    public void replaceCategoryInBudget(Category oldCategory, Category newCategory, CategoryBudget budget) {
        if (oldCategory == null || newCategory == null || budget == null) {
            throw new IllegalArgumentException("Category and budget cannot be null");
        }
        this.categoryBudgets.remove(oldCategory);
        this.categoryBudgets.put(newCategory, budget);
    }
}