package vp.financemanager.infra.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import vp.financemanager.core.models.Wallet;
import vp.financemanager.core.repository.WalletRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileWalletRepository implements WalletRepository {

    private static final String DATA_DIR = "data";

    private final ObjectMapper objectMapper;

    public FileWalletRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        ensureDataDirectory();
    }

    private void ensureDataDirectory() {
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data directory", e);
        }
    }

    @Override
    public Wallet findByOwnerLogin(String login) {
        if (login == null) {
            return null;
        }

        String walletFile = DATA_DIR + "/wallet_" + login + ".json";
        File file = new File(walletFile);
        if (!file.exists()) {
            return null;
        }

        try {
            WalletData walletData = objectMapper.readValue(file, WalletData.class);
            return WalletData.toWallet(walletData);
        } catch (IOException e) {
            return null;
        }
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

        try {
            String walletFile = DATA_DIR + "/wallet_" + ownerLogin + ".json";
            WalletData walletData = WalletData.fromWallet(wallet);
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(walletFile), walletData);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save wallet to file", e);
        }

        return wallet;
    }
}

