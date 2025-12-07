package vp.financemanager.infra.repository;

import vp.financemanager.core.models.Wallet;
import vp.financemanager.core.repository.WalletRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryWalletRepository implements WalletRepository {

    // ключ — логин владельца, значение — кошелек
    private final Map<String, Wallet> wallets = new HashMap<>();

    @Override
    public Wallet findByOwnerLogin(String login) {
        if (login == null) {
            return null;
        }
        return wallets.get(login);
    }

    @Override
    public Wallet save(Wallet wallet) {
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        String ownerLogin = wallet.getOwnerLogin();
        if (ownerLogin == null) {
            throw new IllegalArgumentException("Wallet owner login cannot be null");
        }
        wallets.put(ownerLogin, wallet);
        return wallet;
    }
}