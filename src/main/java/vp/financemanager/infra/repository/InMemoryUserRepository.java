package vp.financemanager.infra.repository;

import vp.financemanager.core.models.User;
import vp.financemanager.core.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> users = new HashMap<>();

    @Override
    public Optional<User> findByLogin(String login) {
        if (login == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(login));
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        users.put(user.getLogin(), user);
        return user;
    }
}