package vp.financemanager.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vp.financemanager.core.models.Category;
import vp.financemanager.core.models.CategoryBudget;
import vp.financemanager.core.models.TransactionType;
import vp.financemanager.core.models.Wallet;
import vp.financemanager.core.repository.WalletRepository;
import vp.financemanager.infra.repository.InMemoryWalletRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {

    private BudgetService budgetService;
    private CategoryService categoryService;
    private WalletRepository walletRepository;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletRepository = new InMemoryWalletRepository();
        categoryService = new CategoryService();
        budgetService = new BudgetService(walletRepository, categoryService);
        wallet = new Wallet("testuser", BigDecimal.ZERO);
    }

    @Test
    void testSetBudget() {
        Category category = new Category("Food");
        budgetService.setBudget(wallet, category, BigDecimal.valueOf(5000));
        
        CategoryBudget budget = wallet.getCategoryBudget(category);
        assertNotNull(budget);
        assertEquals(BigDecimal.valueOf(5000), budget.getLimit());
    }

    @Test
    void testGetRemainingLimit() {
        Category category = new Category("Food");
        budgetService.setBudget(wallet, category, BigDecimal.valueOf(5000));
        
        BigDecimal remaining = budgetService.getRemainingLimit(wallet, category);
        assertEquals(BigDecimal.valueOf(5000), remaining);
    }

    @Test
    void testIsBudgetExceeded() {
        Category category = new Category("Food");
        budgetService.setBudget(wallet, category, BigDecimal.valueOf(1000));
        
        wallet.setBalance(BigDecimal.valueOf(2000));
        wallet.addTransaction(new vp.financemanager.core.models.Transaction(
                TransactionType.EXPENSE,
                BigDecimal.valueOf(1500),
                category,
                "",
                java.time.LocalDateTime.now()
        ));
        wallet.getCategoryBudget(category).addSpent(BigDecimal.valueOf(1500));
        
        assertTrue(budgetService.isBudgetExceeded(wallet, category));
    }

    @Test
    void testCannotSetBudgetForIncomeOnlyCategory() {
        Category category = new Category("Salary");
        wallet.addTransaction(new vp.financemanager.core.models.Transaction(
                TransactionType.INCOME,
                BigDecimal.valueOf(5000),
                category,
                "",
                java.time.LocalDateTime.now()
        ));
        
        assertThrows(IllegalArgumentException.class, () -> {
            budgetService.setBudget(wallet, category, BigDecimal.valueOf(1000));
        });
    }
}

