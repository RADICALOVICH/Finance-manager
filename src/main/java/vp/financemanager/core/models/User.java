package vp.financemanager.core.models;

public class User {
    private final String login;
    private final String passwordHash;
    private final Wallet wallet;

    public User(String login, String passwordHash, Wallet wallet) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login cannot be null or blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or blank");
        }
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }

        this.login = login;
        this.passwordHash = passwordHash;
        this.wallet = wallet;
    }

    public String getLogin() {
        return login;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
