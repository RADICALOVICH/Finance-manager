package vp.financemanager.core.repository;

import vp.financemanager.core.models.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByLogin(String login);

    User save(User user);
}