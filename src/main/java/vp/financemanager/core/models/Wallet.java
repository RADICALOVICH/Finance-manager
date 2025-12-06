package vp.financemanager.core.models;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        return transactions;
    }

    public Map<Category, CategoryBudget> getCategoryBudgets() {
        return categoryBudgets;
    }
}