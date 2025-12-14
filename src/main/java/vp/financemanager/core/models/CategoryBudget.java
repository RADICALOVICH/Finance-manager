package vp.financemanager.core.models;

import java.math.BigDecimal;

public class CategoryBudget {

    private final Category category;
    private BigDecimal limit; // установленный лимит по категории
    private BigDecimal spent;


    public CategoryBudget(Category category, BigDecimal limit) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (limit == null) {
            throw new IllegalArgumentException("Limit cannot be null");
        }
        if (limit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }

        this.category = category;
        this.limit = limit;
        this.spent = BigDecimal.ZERO;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public void addSpent(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be null or negative");
        }
        this.spent = this.spent.add(amount);
    }

    public void setLimit(BigDecimal limit) {
        if (limit == null) {
            throw new IllegalArgumentException("Limit cannot be null");
        }
        if (limit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }
        this.limit = limit;
    }

    public void setSpent(BigDecimal spent) {
        if (spent == null) {
            throw new IllegalArgumentException("Spent cannot be null");
        }
        if (spent.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Spent cannot be negative");
        }
        this.spent = spent;
    }
}