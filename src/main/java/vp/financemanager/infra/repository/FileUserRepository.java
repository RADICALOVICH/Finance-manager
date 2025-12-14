package vp.financemanager.infra.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import vp.financemanager.core.models.User;
import vp.financemanager.core.models.Wallet;
import vp.financemanager.core.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FileUserRepository implements UserRepository {

    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.json";
    private final ObjectMapper objectMapper;
    private final Map<String, User> cache;

    public FileUserRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        this.cache = new HashMap<>();
        ensureDataDirectory();
        loadUsers();
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

    private void loadUsers() {
        File usersFile = new File(USERS_FILE);
        if (!usersFile.exists()) {
            return;
        }

        try {
            UserData[] usersData = objectMapper.readValue(usersFile, UserData[].class);
            for (UserData userData : usersData) {
                Wallet wallet = loadWallet(userData.login);
                User user = new User(userData.login, userData.passwordHash, wallet);
                cache.put(userData.login, user);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load users from file", e);
        }
    }

    private Wallet loadWallet(String login) {
        String walletFile = DATA_DIR + "/wallet_" + login + ".json";
        File file = new File(walletFile);
        if (!file.exists()) {
            return new Wallet(login, BigDecimal.ZERO);
        }

        try {
            WalletData walletData = objectMapper.readValue(file, WalletData.class);
            return WalletData.toWallet(walletData);
        } catch (IOException e) {
            return new Wallet(login, BigDecimal.ZERO);
        }
    }

    private void saveUsers() {
        try {
            UserData[] usersData = cache.values().stream()
                    .map(user -> new UserData(user.getLogin(), user.getPasswordHash()))
                    .toArray(UserData[]::new);
            
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(USERS_FILE), usersData);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save users to file", e);
        }
    }

    @Override
    public Optional<User> findByLogin(String login) {
        if (login == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(cache.get(login));
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        cache.put(user.getLogin(), user);
        saveUsers();
        saveWallet(user.getWallet());
        return user;
    }

    private void saveWallet(Wallet wallet) {
        try {
            String walletFile = DATA_DIR + "/wallet_" + wallet.getOwnerLogin() + ".json";
            WalletData walletData = WalletData.fromWallet(wallet);
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(walletFile), walletData);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save wallet to file", e);
        }
    }

    private static class UserData {
        public String login;
        public String passwordHash;

        public UserData() {
        }

        public UserData(String login, String passwordHash) {
            this.login = login;
            this.passwordHash = passwordHash;
        }
    }
}

