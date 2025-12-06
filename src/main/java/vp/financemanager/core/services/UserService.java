package vp.financemanager.core.services;

import vp.financemanager.core.models.User;
import vp.financemanager.core.models.Wallet;
import vp.financemanager.core.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public User register(String login, String rawPassword, BigDecimal initialBalance){
        Optional<User> existing = userRepository.findByLogin(login);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("User with login '" + login + "' already exists");
        }

        // пароль не должен быть пустым
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }

        String passwordHash = passwordHasher.hash(rawPassword);
        Wallet wallet = new Wallet(initialBalance);
        User user = new User(login, passwordHash, wallet);

        return userRepository.save(user);
    }

    public User login(String login, String rawPassword) {
        Optional<User> userOpt = userRepository.findByLogin(login);
        if (userOpt.isEmpty()) {
            return null; // пользователя с таким логином нет
        }

        User user = userOpt.get();
        boolean matches = passwordHasher.matches(rawPassword, user.getPasswordHash());
        if (!matches) {
            return null; // пароль неверный
        }

        return user; // успешный логин
    }
}
