package vp.financemanager.core.repository;

import vp.financemanager.core.models.Wallet;

public interface WalletRepository {

    Wallet findByOwnerLogin(String login);

    Wallet save(String ownerLogin, Wallet wallet);
}