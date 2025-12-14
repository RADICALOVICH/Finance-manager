package vp.financemanager.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vp.financemanager.core.models.Category;
import vp.financemanager.core.models.User;
import vp.financemanager.core.models.Wallet;
import vp.financemanager.core.service.*;
import vp.financemanager.infra.repository.InMemoryUserRepository;
import vp.financemanager.infra.repository.InMemoryWalletRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FinanceManagerIntegrationTest {

    private UserService userService;
    private WalletService walletService;
    private BudgetService budgetService;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        InMemoryWalletRepository walletRepository = new InMemoryWalletRepository();
        PasswordHasher passwordHasher = new PasswordHasher();

        userService = new UserService(userRepository, passwordHasher);
        categoryService = new CategoryService();
        budgetService = new BudgetService(walletRepository, categoryService);
        walletService = new WalletService(walletRepository, budgetService);
    }

    @Test
    void testFullWorkflow_RegisterAddIncomeAddExpenseSetBudget() {
        User user = userService.register("testuser", "password123", BigDecimal.valueOf(1000));
        Wallet wallet = user.getWallet();

        Category salaryCategory = categoryService.createCategory(wallet, "Salary");
        walletService.addIncome(wallet, BigDecimal.valueOf(50000), salaryCategory, "Monthly salary");

        Category foodCategory = categoryService.createCategory(wallet, "Food");
        walletService.addExpense(wallet, BigDecimal.valueOf(5000), foodCategory, "Groceries");

        budgetService.setBudget(wallet, foodCategory, BigDecimal.valueOf(20000));

        assertEquals(BigDecimal.valueOf(46000), wallet.getBalance());
        assertEquals(2, wallet.getTransactions().size());
        assertNotNull(wallet.getCategoryBudget(foodCategory));
        assertEquals(BigDecimal.valueOf(20000), wallet.getCategoryBudget(foodCategory).getLimit());
    }

    @Test
    void testBudgetExceededWarning() {
        User user = userService.register("testuser", "password123", BigDecimal.ZERO);
        Wallet wallet = user.getWallet();

        Category category = categoryService.createCategory(wallet, "Food");
        budgetService.setBudget(wallet, category, BigDecimal.valueOf(1000));

        walletService.addExpense(wallet, BigDecimal.valueOf(500), category, "");
        walletService.addExpense(wallet, BigDecimal.valueOf(600), category, "");

        assertTrue(budgetService.isBudgetExceeded(wallet, category));
        assertEquals(BigDecimal.valueOf(-100), budgetService.getRemainingLimit(wallet, category));
    }

    @Test
    void testMultipleCategoriesAndBudgets() {
        User user = userService.register("testuser", "password123", BigDecimal.valueOf(10000));
        Wallet wallet = user.getWallet();

        Category foodCategory = categoryService.createCategory(wallet, "Food");
        Category transportCategory = categoryService.createCategory(wallet, "Transport");

        budgetService.setBudget(wallet, foodCategory, BigDecimal.valueOf(5000));
        budgetService.setBudget(wallet, transportCategory, BigDecimal.valueOf(3000));

        walletService.addExpense(wallet, BigDecimal.valueOf(2000), foodCategory, "");
        walletService.addExpense(wallet, BigDecimal.valueOf(1500), transportCategory, "");

        assertEquals(BigDecimal.valueOf(3000), budgetService.getRemainingLimit(wallet, foodCategory));
        assertEquals(BigDecimal.valueOf(1500), budgetService.getRemainingLimit(wallet, transportCategory));
    }

    @Test
    void testIncomeAndExpenseCategories() {
        User user = userService.register("testuser", "password123", BigDecimal.ZERO);
        Wallet wallet = user.getWallet();

        Category salaryCategory = categoryService.createCategory(wallet, "Salary");
        Category foodCategory = categoryService.createCategory(wallet, "Food");

        walletService.addIncome(wallet, BigDecimal.valueOf(10000), salaryCategory, "");
        walletService.addExpense(wallet, BigDecimal.valueOf(3000), foodCategory, "");

        BigDecimal totalIncome = walletService.getTotalIncome(wallet);
        BigDecimal totalExpense = walletService.getTotalExpense(wallet);

        assertEquals(BigDecimal.valueOf(10000), totalIncome);
        assertEquals(BigDecimal.valueOf(3000), totalExpense);
        assertEquals(BigDecimal.valueOf(7000), wallet.getBalance());
    }

    @Test
    void testCategoryReuseAcrossTransactions() {
        User user = userService.register("testuser", "password123", BigDecimal.ZERO);
        Wallet wallet = user.getWallet();

        Category category = categoryService.createCategory(wallet, "Food");
        walletService.addExpense(wallet, BigDecimal.valueOf(100), category, "");
        
        Category foundCategory = categoryService.findCategoryByName(wallet, "Food");
        assertNotNull(foundCategory);
        assertEquals("Food", foundCategory.getName());

        walletService.addExpense(wallet, BigDecimal.valueOf(200), foundCategory, "");

        assertEquals(2, wallet.getTransactions().size());
        assertEquals(BigDecimal.valueOf(300), walletService.getTotalExpense(wallet));
    }
}

