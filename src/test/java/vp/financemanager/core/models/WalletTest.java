package vp.financemanager.core.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @Test
    void testWalletCreation() {
        Wallet wallet = new Wallet("testuser", BigDecimal.valueOf(1000));
        
        assertEquals("testuser", wallet.getOwnerLogin());
        assertEquals(BigDecimal.valueOf(1000), wallet.getBalance());
        assertTrue(wallet.getTransactions().isEmpty());
    }

    @Test
    void testWalletCannotHaveNegativeInitialBalance() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Wallet("testuser", BigDecimal.valueOf(-100));
        });
    }

    @Test
    void testAddTransactionUpdatesBalance() {
        Wallet wallet = new Wallet("testuser", BigDecimal.valueOf(1000));
        Transaction income = new Transaction(
                TransactionType.INCOME,
                BigDecimal.valueOf(500),
                new Category("Salary"),
                "",
                java.time.LocalDateTime.now()
        );
        
        wallet.addTransaction(income);
        
        assertEquals(BigDecimal.valueOf(1500), wallet.getBalance());
        assertEquals(1, wallet.getTransactions().size());
    }
}

