package vp.financemanager.cli;

import vp.financemanager.core.models.Category;
import vp.financemanager.core.models.Transaction;
import vp.financemanager.core.models.TransactionType;
import vp.financemanager.core.models.User;
import vp.financemanager.core.models.Wallet;
import vp.financemanager.core.repository.UserRepository;
import vp.financemanager.core.repository.WalletRepository;
import vp.financemanager.core.service.BudgetService;
import vp.financemanager.core.service.CategoryService;
import vp.financemanager.core.service.PasswordHasher;
import vp.financemanager.core.service.UserService;
import vp.financemanager.core.service.WalletService;
import vp.financemanager.infra.repository.FileUserRepository;
import vp.financemanager.infra.repository.FileWalletRepository;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FinanceCliApp {

    private final Scanner scanner;
    private final UserService userService;
    private final WalletService walletService;
    private final BudgetService budgetService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    // currently logged-in user (null means guest)
    private User currentUser;

    public FinanceCliApp() {
        this.scanner = new Scanner(System.in);

        // infrastructure initialization
        this.userRepository = new FileUserRepository();
        this.walletRepository = new FileWalletRepository();
        PasswordHasher passwordHasher = new PasswordHasher();

        this.userService = new UserService(userRepository, passwordHasher);
        this.categoryService = new CategoryService();
        this.budgetService = new BudgetService(walletRepository, categoryService);
        this.walletService = new WalletService(walletRepository, budgetService);
    }

    public static void main(String[] args) {
        FinanceCliApp app = new FinanceCliApp();
        app.run();
    }

    public void run() {
        System.out.println("=== Personal Finance Manager ===");
        System.out.println("Simple CLI. Type 'help' to see available commands.\n");

        boolean running = true;
        while (running) {
            printPrompt();
            String line = scanner.nextLine();
            if (line == null) {
                break;
            }

            String command = line.trim().toLowerCase();
            switch (command) {
                case "help":
                    printHelp();
                    break;
                case "register":
                    handleRegister();
                    break;
                case "login":
                    handleLogin();
                    break;
                case "logout":
                    handleLogout();
                    break;
                case "add_income":
                    handleAddIncome();
                    break;
                case "add_expense":
                    handleAddExpense();
                    break;
                case "set_budget":
                    handleSetBudget();
                    break;
                case "show_budgets":
                    handleShowBudgets();
                    break;
                case "show_categories":
                    handleShowCategories();
                    break;
                case "show_summary":
                    handleShowSummary();
                    break;
                case "show_transactions":
                    handleShowTransactions();
                    break;
                case "export_transactions":
                    handleExportTransactions();
                    break;
                case "import_transactions":
                    handleImportTransactions();
                    break;
                case "rename_category":
                    handleRenameCategory();
                    break;
                case "exit":
                    saveAllData();
                    running = false;
                    break;
                default:
                    System.out.println("Unknown command. Type 'help' to see available commands.");
            }
        }

        System.out.println("Exiting application. Goodbye!");
    }

    private void saveAllData() {
        if (currentUser != null) {
            userRepository.save(currentUser);
            walletRepository.save(currentUser.getWallet());
        }
    }

    private void printPrompt() {
        if (currentUser == null) {
            System.out.print("[guest] > ");
        } else {
            System.out.print("[" + currentUser.getLogin() + "] > ");
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help           - show this help message");
        System.out.println("  register       - register a new user");
        System.out.println("  login          - login as an existing user");
        System.out.println("  logout         - logout current user");
        System.out.println("  add_income     - add income transaction for current user");
        System.out.println("  add_expense    - add expense transaction for current user");
        System.out.println("  set_budget     - set budget for a category");
        System.out.println("  show_budgets   - show budgets and remaining limits");
        System.out.println("  show_categories- list all categories with budgets");
        System.out.println("  show_summary   - show income/expenses summary");
        System.out.println("  show_transactions - show transactions with filters (category, date range)");
        System.out.println("  export_transactions - export transactions to CSV file");
        System.out.println("  import_transactions - import transactions from CSV file");
        System.out.println("  rename_category - rename a category (updates all transactions and budgets)");
        System.out.println("  exit           - exit the application");
    }

    private boolean ensureLoggedIn() {
        if (currentUser == null) {
            System.out.println("You must be logged in to use this command. Use 'login' or 'register' first.");
            return false;
        }
        return true;
    }

    private void handleRegister() {
        System.out.println("--- User registration ---");
        System.out.print("Login: ");
        String login = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("Initial balance (e.g. 0 or 1000): ");
        String balanceInput = scanner.nextLine();

        BigDecimal initialBalance;
        try {
            initialBalance = new BigDecimal(balanceInput.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format. Registration cancelled.");
            return;
        }

        try {
            User user = userService.register(login, password, initialBalance);
            currentUser = user; // Автоматический вход после регистрации
            System.out.println("User '" + user.getLogin() + "' registered successfully. You are now logged in.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Registration error: " + ex.getMessage());
        }
    }

    private void handleLogin() {
        if (currentUser != null) {
            System.out.println("You are already logged in as '" + currentUser.getLogin() + "'. Please logout first.");
            return;
        }

        System.out.println("--- Login ---");
        System.out.print("Login: ");
        String login = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userService.login(login, password);
        if (user == null) {
            System.out.println("Invalid login or password.");
        } else {
            currentUser = user;
            System.out.println("Login successful. Current user: " + currentUser.getLogin());
        }
    }

    private void handleLogout() {
        if (currentUser == null) {
            System.out.println("No user is currently logged in.");
        } else {
            System.out.println("Logging out user: " + currentUser.getLogin());
            currentUser = null;
        }
    }

    private void handleAddIncome() {
        if (!ensureLoggedIn()) {
            return;
        }

        Wallet wallet = currentUser.getWallet();

        System.out.println("--- Add income ---");
        System.out.print("Amount: ");
        String amountInput = scanner.nextLine();

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountInput.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format. Income not added.");
            return;
        }

        System.out.print("Category name: ");
        String categoryName = scanner.nextLine();
        Category category = categoryService.createCategory(wallet, categoryName);

        System.out.print("Description (optional): ");
        String description = scanner.nextLine();

        try {
            walletService.addIncome(wallet, amount, category, description);
            System.out.println("Income added successfully.");

            // Zero balance alert
            if (wallet.getBalance().compareTo(BigDecimal.ZERO) == 0) {
                System.out.println("WARNING: Wallet balance is zero.");
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("Error adding income: " + ex.getMessage());
        }
    }

    private void handleAddExpense() {
        if (!ensureLoggedIn()) {
            return;
        }

        Wallet wallet = currentUser.getWallet();

        System.out.println("--- Add expense ---");
        System.out.print("Amount: ");
        String amountInput = scanner.nextLine();

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountInput.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format. Expense not added.");
            return;
        }

        System.out.print("Category name: ");
        String categoryName = scanner.nextLine();
        Category category = categoryService.createCategory(wallet, categoryName);

        System.out.print("Description (optional): ");
        String description = scanner.nextLine();

        try {
            walletService.addExpense(wallet, amount, category, description);
            System.out.println("Expense added successfully.");

            // Budget alerts (if category has a budget)
            try {
                if (budgetService.isBudgetExceeded(wallet, category)) {
                    System.out.println("WARNING: Budget limit exceeded for category '" + category.getName() + "'.");
                } else if (budgetService.isBudgetNearLimit(wallet, category)) {
                    System.out.println("WARNING: Budget for category '" + category.getName() + "' is at 80% or more of the limit.");
                }
            } catch (IllegalStateException ignored) {
                // No budget set for this category — silently ignore for now
            }

            // Zero balance alert
            if (wallet.getBalance().compareTo(BigDecimal.ZERO) == 0) {
                System.out.println("WARNING: Wallet balance is zero.");
            }

            // Total income vs total expense alert
            BigDecimal totalIncome = walletService.getTotalIncome(wallet);
            BigDecimal totalExpense = walletService.getTotalExpense(wallet);
            if (totalExpense.compareTo(totalIncome) > 0) {
                System.out.println("WARNING: Total expenses are greater than total income.");
            }

        } catch (IllegalArgumentException ex) {
            System.out.println("Error adding expense: " + ex.getMessage());
        }
    }

    private void handleSetBudget() {
        if (!ensureLoggedIn()) {
            return;
        }

        Wallet wallet = currentUser.getWallet();

        System.out.println("--- Set budget for category ---");
        System.out.print("Category name: ");
        String categoryName = scanner.nextLine();

        Category category = categoryService.createCategory(wallet, categoryName);

        System.out.print("Budget limit (amount): ");
        String limitInput = scanner.nextLine();

        BigDecimal limit;
        try {
            limit = new BigDecimal(limitInput.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format. Budget not set.");
            return;
        }

        try {
            budgetService.setBudget(wallet, category, limit);
            System.out.println("Budget set for category '" + category.getName() + "': " + limit);
        } catch (IllegalArgumentException ex) {
            System.out.println("Error setting budget: " + ex.getMessage());
        }
    }

    private void handleShowBudgets() {
        if (!ensureLoggedIn()) {
            return;
        }

        Wallet wallet = currentUser.getWallet();

        System.out.println("--- Budgets by category ---");

        var budgets = budgetService.getAllBudgets(wallet);
        if (budgets.isEmpty()) {
            System.out.println("No budgets set.");
            return;
        }

        for (var budget : budgets) {
            Category category = budget.getCategory();
            BigDecimal limit = budget.getLimit();
            BigDecimal spent = budget.getSpent();

            BigDecimal remaining;
            try {
                remaining = budgetService.getRemainingLimit(wallet, category);
            } catch (IllegalStateException e) {
                // Should not happen for budgets returned by getAllBudgets,
                // but just in case:
                remaining = limit.subtract(spent);
            }

            System.out.println(
                    category.getName()
                            + ": limit = " + limit
                            + ", spent = " + spent
                            + ", remaining = " + remaining
            );
        }
    }

    private void handleShowCategories() {
        if (!ensureLoggedIn()) {
            return;
        }

        Wallet wallet = currentUser.getWallet();

        System.out.println("--- Categories (with budgets) ---");

        var categories = categoryService.getAllCategories(wallet);
        if (categories.isEmpty()) {
            System.out.println("No categories with budgets yet.");
            return;
        }

        for (Category category : categories) {
            System.out.println(" - " + category.getName());
        }
    }

    private void handleShowSummary() {
        if (!ensureLoggedIn()) {
            return;
        }

        Wallet wallet = currentUser.getWallet();

        System.out.println("--- Summary ---");

        // 1. Общий доход
        BigDecimal totalIncome = walletService.getTotalIncome(wallet);
        System.out.println("Общий доход: " + totalIncome);

        // 2. Доходы по категориям
        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        wallet.getTransactions().forEach(tx -> {
            if (tx.getType() == TransactionType.INCOME) {
                    String catName = tx.getCategory().getName();
                    incomeByCategory.merge(catName, tx.getAmount(), BigDecimal::add);
            }
        });

        if (!incomeByCategory.isEmpty()) {
            System.out.println("Доходы по категориям:");
            incomeByCategory.forEach((catName, amount) ->
                    System.out.println("  " + catName + ": " + amount)
            );
        }

        // 3. Общие расходы
        BigDecimal totalExpense = walletService.getTotalExpense(wallet);
        System.out.println("Общие расходы: " + totalExpense);

        // 4. Бюджет по категориям (используем логику из handleShowBudgets)
        var budgets = budgetService.getAllBudgets(wallet);
        if (!budgets.isEmpty()) {
            System.out.println("Бюджет по категориям:");
            for (var budget : budgets) {
                Category category = budget.getCategory();
                BigDecimal limit = budget.getLimit();
                
                BigDecimal remaining;
                try {
                    remaining = budgetService.getRemainingLimit(wallet, category);
                } catch (IllegalStateException e) {
                    // Should not happen for budgets returned by getAllBudgets
                    remaining = limit.subtract(budget.getSpent());
                }

                System.out.println("  " + category.getName() 
                        + ": " + limit 
                        + ", Оставшийся бюджет: " + remaining);
            }
        }
    }

    private void handleShowTransactions() {
        if (!ensureLoggedIn()) {
            return;
        }

        Wallet wallet = currentUser.getWallet();

        System.out.println("--- Show Transactions ---");
        System.out.print("Type (income/expense/all, default: all): ");
        String typeInput = scanner.nextLine().trim().toLowerCase();
        
        TransactionType type = null;
        if ("income".equals(typeInput)) {
            type = TransactionType.INCOME;
        } else if ("expense".equals(typeInput)) {
            type = TransactionType.EXPENSE;
        }

        System.out.print("Categories (comma-separated, empty for all): ");
        String categoriesInput = scanner.nextLine().trim();
        
        List<Category> categories = null;
        if (!categoriesInput.isEmpty()) {
            categories = new ArrayList<>();
            String[] categoryNames = categoriesInput.split(",");
            for (String catName : categoryNames) {
                Category existing = categoryService.findCategoryByName(wallet, catName.trim());
                if (existing != null) {
                    categories.add(existing);
                } else {
                    System.out.println("Warning: Category '" + catName.trim() + "' not found, skipping.");
                }
            }
            if (categories.isEmpty()) {
                System.out.println("No valid categories found. Showing all transactions.");
                categories = null;
            }
        }

        System.out.print("From date (YYYY-MM-DD, empty for no limit): ");
        String fromDateInput = scanner.nextLine().trim();
        LocalDate fromDate = parseDate(fromDateInput);
        if (fromDateInput != null && !fromDateInput.isEmpty() && fromDate == null) {
            System.out.println("Invalid date format. Ignoring from date filter.");
        }

        System.out.print("To date (YYYY-MM-DD, empty for no limit): ");
        String toDateInput = scanner.nextLine().trim();
        LocalDate toDate = parseDate(toDateInput);
        if (toDateInput != null && !toDateInput.isEmpty() && toDate == null) {
            System.out.println("Invalid date format. Ignoring to date filter.");
        }

        List<Transaction> transactions = walletService.getTransactions(wallet, type, categories, fromDate, toDate);

        if (transactions.isEmpty()) {
            System.out.println("No transactions found matching the criteria.");
            return;
        }

        System.out.println("\nTransactions:");
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        
        for (Transaction tx : transactions) {
            System.out.println(String.format("  %s | %s | %s | %s | %s",
                    tx.getTimestamp().toLocalDate(),
                    tx.getType(),
                    tx.getCategory().getName(),
                    tx.getAmount(),
                    tx.getDescription() != null ? tx.getDescription() : ""));
            
            if (tx.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(tx.getAmount());
            } else {
                totalExpense = totalExpense.add(tx.getAmount());
            }
        }
        
        System.out.println("\nTotal: " + transactions.size() + " transaction(s)");
        if (type == null || type == TransactionType.INCOME) {
            System.out.println("Total income (filtered): " + totalIncome);
        }
        if (type == null || type == TransactionType.EXPENSE) {
            System.out.println("Total expense (filtered): " + totalExpense);
        }
    }

    private void handleExportTransactions() {
        if (!ensureLoggedIn()) {
            return;
        }

        Wallet wallet = currentUser.getWallet();

        System.out.println("--- Export Transactions to CSV ---");
        System.out.print("Type (income/expense/all, default: all): ");
        String typeInput = scanner.nextLine().trim().toLowerCase();
        
        TransactionType type = null;
        if ("income".equals(typeInput)) {
            type = TransactionType.INCOME;
        } else if ("expense".equals(typeInput)) {
            type = TransactionType.EXPENSE;
        }

        System.out.print("Categories (comma-separated, empty for all): ");
        String categoriesInput = scanner.nextLine().trim();
        
        List<Category> categories = null;
        if (!categoriesInput.isEmpty()) {
            categories = new ArrayList<>();
            String[] categoryNames = categoriesInput.split(",");
            for (String catName : categoryNames) {
                Category existing = categoryService.findCategoryByName(wallet, catName.trim());
                if (existing != null) {
                    categories.add(existing);
                } else {
                    System.out.println("Warning: Category '" + catName.trim() + "' not found, skipping.");
                }
            }
            if (categories.isEmpty()) {
                System.out.println("No valid categories found. Exporting all transactions.");
                categories = null;
            }
        }

        System.out.print("From date (YYYY-MM-DD, empty for no limit): ");
        String fromDateInput = scanner.nextLine().trim();
        LocalDate fromDate = parseDate(fromDateInput);
        if (fromDateInput != null && !fromDateInput.isEmpty() && fromDate == null) {
            System.out.println("Invalid date format. Ignoring from date filter.");
        }

        System.out.print("To date (YYYY-MM-DD, empty for no limit): ");
        String toDateInput = scanner.nextLine().trim();
        LocalDate toDate = parseDate(toDateInput);
        if (toDateInput != null && !toDateInput.isEmpty() && toDate == null) {
            System.out.println("Invalid date format. Ignoring to date filter.");
        }

        System.out.print("Output file name (default: transactions_export.csv): ");
        String fileName = scanner.nextLine().trim();
        if (fileName.isEmpty()) {
            fileName = "transactions_export.csv";
        }
        if (!fileName.endsWith(".csv")) {
            fileName += ".csv";
        }

        List<Transaction> transactions = walletService.getTransactions(wallet, type, categories, fromDate, toDate);

        if (transactions.isEmpty()) {
            System.out.println("No transactions found matching the criteria. Export cancelled.");
            return;
        }

        try (FileWriter writer = new FileWriter(fileName)) {
            walletService.exportTransactionsToCsv(wallet, type, categories, fromDate, toDate, writer);
            System.out.println("Exported " + transactions.size() + " transaction(s) to " + fileName);
        } catch (IOException e) {
            System.out.println("Error exporting transactions: " + e.getMessage());
        }
    }

    private void handleImportTransactions() {
        if (!ensureLoggedIn()) {
            return;
        }

        Wallet wallet = currentUser.getWallet();

        System.out.println("--- Import Transactions from CSV ---");
        System.out.print("CSV file name: ");
        String fileName = scanner.nextLine().trim();
        
        if (fileName.isEmpty()) {
            System.out.println("File name cannot be empty.");
            return;
        }

        try (FileReader reader = new FileReader(fileName)) {
            List<String> errors = walletService.importTransactionsFromCsv(wallet, reader, categoryService);
            
            if (errors.isEmpty()) {
                System.out.println("Transactions imported successfully.");
            } else {
                System.out.println("Import completed with " + errors.size() + " error(s):");
                for (String error : errors) {
                    System.out.println("  - " + error);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    private void handleRenameCategory() {
        if (!ensureLoggedIn()) {
            return;
        }

        Wallet wallet = currentUser.getWallet();

        System.out.println("--- Rename Category ---");
        System.out.print("Current category name: ");
        String oldCategoryName = scanner.nextLine().trim();
        
        if (oldCategoryName.isEmpty()) {
            System.out.println("Category name cannot be empty.");
            return;
        }

        Category oldCategory = categoryService.findCategoryByName(wallet, oldCategoryName);
        if (oldCategory == null) {
            System.out.println("Category '" + oldCategoryName + "' not found.");
            return;
        }

        System.out.print("New category name: ");
        String newCategoryName = scanner.nextLine().trim();
        
        if (newCategoryName.isEmpty()) {
            System.out.println("New category name cannot be empty.");
            return;
        }

        try {
            categoryService.renameCategory(wallet, oldCategory, newCategoryName, walletRepository);
            System.out.println("Category renamed from '" + oldCategory.getName() + "' to '" + newCategoryName + "'.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Error renaming category: " + ex.getMessage());
        }
    }

    private LocalDate parseDate(String dateInput) {
        if (dateInput == null || dateInput.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateInput.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}