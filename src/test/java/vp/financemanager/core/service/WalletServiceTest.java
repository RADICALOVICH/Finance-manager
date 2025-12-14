package vp.financemanager.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vp.financemanager.core.models.Category;
import vp.financemanager.core.models.TransactionType;
import vp.financemanager.core.models.Wallet;
import vp.financemanager.core.repository.WalletRepository;
import vp.financemanager.infra.repository.InMemoryWalletRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class WalletServiceTest {

    private WalletService walletService;
    private BudgetService budgetService;
    private CategoryService categoryService;
    private WalletRepository walletRepository;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletRepository = new InMemoryWalletRepository();
        categoryService = new CategoryService();
        budgetService = new BudgetService(walletRepository, categoryService);
        walletService = new WalletService(walletRepository, budgetService);
        wallet = new Wallet("testuser", BigDecimal.ZERO);
    }

    @Test
    void testAddIncome() {
        Category category = new Category("Salary");
        walletService.addIncome(wallet, BigDecimal.valueOf(5000), category, "Monthly salary");
        
        assertEquals(BigDecimal.valueOf(5000), wallet.getBalance());
        assertEquals(1, wallet.getTransactions().size());
        assertEquals(TransactionType.INCOME, wallet.getTransactions().get(0).getType());
    }

    @Test
    void testAddExpense() {
        Category category = new Category("Food");
        wallet.setBalance(BigDecimal.valueOf(1000));
        walletService.addExpense(wallet, BigDecimal.valueOf(200), category, "Groceries");
        
        assertEquals(BigDecimal.valueOf(800), wallet.getBalance());
        assertEquals(1, wallet.getTransactions().size());
        assertEquals(TransactionType.EXPENSE, wallet.getTransactions().get(0).getType());
    }

    @Test
    void testAddExpenseCreatesBudget() {
        Category category = new Category("Food");
        wallet.setBalance(BigDecimal.valueOf(1000));
        walletService.addExpense(wallet, BigDecimal.valueOf(200), category, "Groceries");
        
        assertNotNull(wallet.getCategoryBudget(category));
        assertEquals(BigDecimal.valueOf(200), wallet.getCategoryBudget(category).getSpent());
    }

    @Test
    void testGetTotalIncome() {
        Category category1 = new Category("Salary");
        Category category2 = new Category("Bonus");
        
        walletService.addIncome(wallet, BigDecimal.valueOf(5000), category1, "");
        walletService.addIncome(wallet, BigDecimal.valueOf(1000), category2, "");
        
        BigDecimal total = walletService.getTotalIncome(wallet);
        assertEquals(BigDecimal.valueOf(6000), total);
    }

    @Test
    void testGetTotalExpense() {
        Category category = new Category("Food");
        wallet.setBalance(BigDecimal.valueOf(1000));
        
        walletService.addExpense(wallet, BigDecimal.valueOf(200), category, "");
        walletService.addExpense(wallet, BigDecimal.valueOf(300), category, "");
        
        BigDecimal total = walletService.getTotalExpense(wallet);
        assertEquals(BigDecimal.valueOf(500), total);
    }

    @Test
    void testAddIncomeWithNullAmount() {
        Category category = new Category("Salary");
        
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.addIncome(wallet, null, category, "");
        });
    }

    @Test
    void testAddExpenseWithZeroAmount() {
        Category category = new Category("Food");
        wallet.setBalance(BigDecimal.valueOf(1000));
        
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.addExpense(wallet, BigDecimal.ZERO, category, "");
        });
    }
}

