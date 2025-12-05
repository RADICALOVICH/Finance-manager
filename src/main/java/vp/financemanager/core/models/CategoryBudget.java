package vp.financemanager.core.models;

import java.math.BigDecimal;

public class CategoryBudget {

    private final Category category;
    private BigDecimal limit; // установленный лимит по категории

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
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getLimit() {
        return limit;
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
}