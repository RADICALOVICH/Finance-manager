package vp.financemanager.infra.repository;

import vp.financemanager.core.models.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletData {
    public String ownerLogin;
    public BigDecimal balance;
    public List<TransactionData> transactions;
    public Map<String, CategoryBudgetData> categoryBudgets;

    public WalletData() {
        this.transactions = new ArrayList<>();
        this.categoryBudgets = new HashMap<>();
    }

    public static WalletData fromWallet(Wallet wallet) {
        WalletData data = new WalletData();
        data.ownerLogin = wallet.getOwnerLogin();
        data.balance = wallet.getBalance();
        
        for (Transaction tx : wallet.getTransactions()) {
            data.transactions.add(TransactionData.fromTransaction(tx));
        }
        
        for (Map.Entry<Category, CategoryBudget> entry : wallet.getCategoryBudgets().entrySet()) {
            String categoryName = entry.getKey().getName();
            data.categoryBudgets.put(categoryName, CategoryBudgetData.fromBudget(entry.getValue()));
        }
        
        return data;
    }

    public static Wallet toWallet(WalletData data) {
        Wallet wallet = new Wallet(data.ownerLogin, BigDecimal.ZERO);
        wallet.restoreBalance(data.balance);
        
        for (TransactionData txData : data.transactions) {
            Transaction tx = TransactionData.toTransaction(txData);
            wallet.restoreTransaction(tx);
        }
        
        for (Map.Entry<String, CategoryBudgetData> entry : data.categoryBudgets.entrySet()) {
            Category category = new Category(entry.getKey());
            CategoryBudget budget = CategoryBudgetData.toBudget(entry.getValue(), category);
            wallet.addCategoryBudget(category, budget);
        }
        
        return wallet;
    }

    static class TransactionData {
        public String type;
        public BigDecimal amount;
        public String categoryName;
        public String description;
        public LocalDateTime timestamp;

        public TransactionData() {
        }

        static TransactionData fromTransaction(Transaction tx) {
            TransactionData data = new TransactionData();
            data.type = tx.getType().name();
            data.amount = tx.getAmount();
            data.categoryName = tx.getCategory().getName();
            data.description = tx.getDescription();
            data.timestamp = tx.getTimestamp();
            return data;
        }

        static Transaction toTransaction(TransactionData data) {
            TransactionType type = TransactionType.valueOf(data.type);
            Category category = new Category(data.categoryName);
            return new Transaction(type, data.amount, category, data.description, data.timestamp);
        }
    }

    static class CategoryBudgetData {
        public BigDecimal limit;
        public BigDecimal spent;

        public CategoryBudgetData() {
        }

        static CategoryBudgetData fromBudget(CategoryBudget budget) {
            CategoryBudgetData data = new CategoryBudgetData();
            data.limit = budget.getLimit();
            data.spent = budget.getSpent();
            return data;
        }

        static CategoryBudget toBudget(CategoryBudgetData data, Category category) {
            CategoryBudget budget = new CategoryBudget(category, data.limit);
            budget.setSpent(data.spent);
            return budget;
        }
    }
}

