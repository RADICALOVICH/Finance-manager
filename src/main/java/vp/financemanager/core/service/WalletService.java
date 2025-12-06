package vp.financemanager.core.service;

import vp.financemanager.core.models.Category;
import vp.financemanager.core.models.Transaction;
import vp.financemanager.core.models.TransactionType;
import vp.financemanager.core.models.Wallet;
import vp.financemanager.core.repository.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletService {
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
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

        wallet.getTransactions().add(income);
        wallet.setBalance(wallet.getBalance().add(amount));
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

        Transaction expense = new Transaction(
                TransactionType.EXPENSE,
                amount,
                category,
                description,
                LocalDateTime.now());

        wallet.getTransactions().add(expense);
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }


}
