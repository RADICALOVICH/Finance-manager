package vp.financemanager.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vp.financemanager.core.models.Category;
import vp.financemanager.core.models.Wallet;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CategoryServiceTest {

    private CategoryService categoryService;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService();
        wallet = new Wallet("testuser", BigDecimal.ZERO);
    }

    @Test
    void testCreateCategory() {
        Category category = categoryService.createCategory(wallet, "Food");
        
        assertNotNull(category);
        assertEquals("Food", category.getName());
    }

    @Test
    void testFindCategoryByName() {
        Category category = categoryService.createCategory(wallet, "Food");
        wallet.addCategoryBudget(category, new vp.financemanager.core.models.CategoryBudget(category, BigDecimal.ZERO));
        
        Category found = categoryService.findCategoryByName(wallet, "Food");
        assertNotNull(found);
        assertEquals("Food", found.getName());
    }

    @Test
    void testFindCategoryCaseInsensitive() {
        Category category = categoryService.createCategory(wallet, "Food");
        wallet.addCategoryBudget(category, new vp.financemanager.core.models.CategoryBudget(category, BigDecimal.ZERO));
        
        Category found = categoryService.findCategoryByName(wallet, "food");
        assertNotNull(found);
        assertEquals("Food", found.getName());
    }
}

