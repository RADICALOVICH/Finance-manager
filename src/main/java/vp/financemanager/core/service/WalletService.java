package vp.financemanager.core.service;

import vp.financemanager.core.models.*;
import vp.financemanager.core.repository.WalletRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WalletService {
    private final WalletRepository walletRepository;
    private final BudgetService budgetService;

    public WalletService(WalletRepository walletRepository, BudgetService budgetService) {
        this.walletRepository = walletRepository;
        this.budgetService = budgetService;
    }

    public void addIncome(Wallet wallet, BigDecimal amount, Category category, String description) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Income must be greater than 0");
        }

        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        Transaction income = new Transaction(
                TransactionType.INCOME,
                amount,
                category,
                description,
                LocalDateTime.now()
        );

        wallet.addTransaction(income);
        walletRepository.save(wallet);
    }

    public void addExpense(Wallet wallet, BigDecimal amount, Category category, String description){
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense must be greater than 0");
        }

        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        // Убеждаемся, что бюджет существует для категории расходов
        budgetService.ensureBudgetExists(wallet, category);
        
        // Находим правильную категорию (из budgets, если есть)
        Category existingCategory = category;
        CategoryBudget budget = wallet.getCategoryBudget(category);
        if (budget != null) {
            existingCategory = budget.getCategory();
        }
        
        // Создаем транзакцию с правильной категорией
        Transaction expense = new Transaction(
                TransactionType.EXPENSE,
                amount,
                existingCategory,
                description,
                LocalDateTime.now());
        
        wallet.addTransaction(expense);
        
        // Обновляем spent в бюджете
        CategoryBudget categoryBudget = wallet.getCategoryBudget(existingCategory);
        if (categoryBudget != null) {
            categoryBudget.addSpent(amount);
        }

        walletRepository.save(wallet);
    }

    public BigDecimal getTotalIncome(Wallet wallet) {
        return getTotalByTypeAndCategories(wallet, TransactionType.INCOME, null);
    }

    public BigDecimal getTotalExpense(Wallet wallet) {
        return getTotalByTypeAndCategories(wallet, TransactionType.EXPENSE, null);
    }

    public BigDecimal getTotalIncomeByCategory(Wallet wallet, Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return getTotalByTypeAndCategories(wallet, TransactionType.INCOME, List.of(category));
    }

    public BigDecimal getTotalExpenseByCategory(Wallet wallet, Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return getTotalByTypeAndCategories(wallet, TransactionType.EXPENSE, List.of(category));
    }


    public BigDecimal getTotalByCategories(Wallet wallet,
                                           List<Category> categories,
                                           TransactionType type) {
        return getTotalByTypeAndCategories(wallet, type, categories);
    }

    private BigDecimal getTotalByTypeAndCategories(Wallet wallet,
                                                   TransactionType type,
                                                   List<Category> categoriesOrNull) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }

        boolean filterByCategory = categoriesOrNull != null && !categoriesOrNull.isEmpty();

        BigDecimal total = BigDecimal.ZERO;

        for (Transaction tx : wallet.getTransactions()) {
            if (tx.getType() != type) {
                continue;
            }

            if (filterByCategory && !categoriesOrNull.contains(tx.getCategory())) {
                continue;
            }

            total = total.add(tx.getAmount());
        }

        return total;
    }

    public List<Transaction> getTransactions(Wallet wallet, 
                                             TransactionType type,
                                             List<Category> categories,
                                             LocalDate fromDate,
                                             LocalDate toDate) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }

        return wallet.getTransactions().stream()
                .filter(tx -> type == null || tx.getType() == type)
                .filter(tx -> categories == null || categories.isEmpty() || categories.contains(tx.getCategory()))
                .filter(tx -> {
                    if (fromDate == null && toDate == null) {
                        return true;
                    }
                    LocalDate txDate = tx.getTimestamp().toLocalDate();
                    boolean afterFrom = fromDate == null || !txDate.isBefore(fromDate);
                    boolean beforeTo = toDate == null || !txDate.isAfter(toDate);
                    return afterFrom && beforeTo;
                })
                .collect(Collectors.toList());
    }

    public void exportTransactionsToCsv(Wallet wallet,
                                       TransactionType type,
                                       List<Category> categories,
                                       LocalDate fromDate,
                                       LocalDate toDate,
                                       Writer writer) throws IOException {
        List<Transaction> transactions = getTransactions(wallet, type, categories, fromDate, toDate);
        
        writer.write("Date,Type,Category,Amount,Description\n");
        
        for (Transaction tx : transactions) {
            String date = tx.getTimestamp().toLocalDate().toString();
            String txType = tx.getType().name();
            String category = tx.getCategory().getName();
            String amount = tx.getAmount().toString();
            String description = tx.getDescription() != null ? tx.getDescription() : "";
            
            description = description.replace("\"", "\"\"").replace(",", ";");
            
            writer.write(String.format("%s,%s,%s,%s,\"%s\"\n",
                    date, txType, category, amount, description));
        }
    }

    public List<String> importTransactionsFromCsv(Wallet wallet, Reader reader, CategoryService categoryService) throws IOException {
        List<String> errors = new ArrayList<>();
        BufferedReader br = new BufferedReader(reader);
        
        String header = br.readLine();
        if (header == null || !header.trim().equals("Date,Type,Category,Amount,Description")) {
            errors.add("Invalid CSV header. Expected: Date,Type,Category,Amount,Description");
            return errors;
        }
        
        String line;
        int lineNumber = 1;
        int importedCount = 0;
        
        while ((line = br.readLine()) != null) {
            lineNumber++;
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            
            try {
                String[] parts = parseCsvLine(line);
                if (parts.length < 4) {
                    errors.add("Line " + lineNumber + ": Invalid format (expected at least 4 fields)");
                    continue;
                }
                
                LocalDate date = LocalDate.parse(parts[0]);
                TransactionType type = TransactionType.valueOf(parts[1]);
                String categoryName = parts[2];
                BigDecimal amount = new BigDecimal(parts[3]);
                String description = parts.length > 4 ? parts[4].replace("\"\"", "\"") : "";
                
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add("Line " + lineNumber + ": Amount must be greater than 0");
                    continue;
                }
                
                Category category = categoryService.createCategory(wallet, categoryName);
                LocalDateTime timestamp = date.atTime(LocalTime.MIDNIGHT);
                
                if (type == TransactionType.INCOME) {
                    Transaction transaction = new Transaction(type, amount, category, description, timestamp);
                    wallet.addTransaction(transaction);
                } else {
                    budgetService.ensureBudgetExists(wallet, category);
                    Category existingCategory = category;
                    CategoryBudget budget = wallet.getCategoryBudget(category);
                    if (budget != null) {
                        existingCategory = budget.getCategory();
                    }
                    
                    Transaction transaction = new Transaction(type, amount, existingCategory, description, timestamp);
                    wallet.addTransaction(transaction);
                    
                    CategoryBudget categoryBudget = wallet.getCategoryBudget(existingCategory);
                    if (categoryBudget != null) {
                        categoryBudget.addSpent(amount);
                    }
                }
                
                importedCount++;
            } catch (Exception e) {
                errors.add("Line " + lineNumber + ": " + e.getMessage());
            }
        }
        
        if (importedCount > 0) {
            walletRepository.save(wallet);
        }
        
        return errors;
    }
    
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }
}
