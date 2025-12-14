package vp.financemanager.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vp.financemanager.core.models.User;
import vp.financemanager.core.repository.UserRepository;
import vp.financemanager.infra.repository.InMemoryUserRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private PasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        passwordHasher = new PasswordHasher();
        userService = new UserService(userRepository, passwordHasher);
    }

    @Test
    void testRegisterNewUser() {
        User user = userService.register("testuser", "password123", BigDecimal.valueOf(1000));
        
        assertNotNull(user);
        assertEquals("testuser", user.getLogin());
        assertNotNull(user.getWallet());
        assertEquals(BigDecimal.valueOf(1000), user.getWallet().getBalance());
    }

    @Test
    void testRegisterDuplicateUser() {
        userService.register("testuser", "password123", BigDecimal.ZERO);
        
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register("testuser", "password456", BigDecimal.ZERO);
        });
    }

    @Test
    void testRegisterWithEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register("testuser", "", BigDecimal.ZERO);
        });
    }

    @Test
    void testLoginWithCorrectCredentials() {
        userService.register("testuser", "password123", BigDecimal.ZERO);
        
        User loggedIn = userService.login("testuser", "password123");
        
        assertNotNull(loggedIn);
        assertEquals("testuser", loggedIn.getLogin());
    }

    @Test
    void testLoginWithWrongPassword() {
        userService.register("testuser", "password123", BigDecimal.ZERO);
        
        User loggedIn = userService.login("testuser", "wrongpassword");
        
        assertNull(loggedIn);
    }

    @Test
    void testLoginWithNonExistentUser() {
        User loggedIn = userService.login("nonexistent", "password123");
        
        assertNull(loggedIn);
    }
}

