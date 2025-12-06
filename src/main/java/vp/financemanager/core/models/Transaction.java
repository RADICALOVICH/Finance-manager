package vp.financemanager.core.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {

    private final TransactionType type;
    private final BigDecimal amount;         // сумма операции
    private final Category category;           // категория (например, "Еда", "Зарплата")
    private final String description;        // комментарий (может быть null или пустой)
    private final LocalDateTime timestamp;   // время операции

    public Transaction(
            TransactionType type,
            BigDecimal amount,
            Category category,
            String description,
            LocalDateTime timestamp
    ) {
        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }

        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.timestamp = timestamp;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "type=" + type +
                ", amount=" + amount +
                ", category='" + category.getName() + '\'' +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return type == that.type
                && Objects.equals(amount, that.amount)
                && Objects.equals(category, that.category)
                && Objects.equals(description, that.description)
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount, category, description, timestamp);
    }
}