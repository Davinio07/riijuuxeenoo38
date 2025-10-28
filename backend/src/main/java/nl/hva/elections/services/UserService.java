package nl.hva.elections.services;

import nl.hva.elections.models.User;
import nl.hva.elections.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing users.
 * This class contains all the business logic for user-related stuff,
 * like creating new users and checking their login info.
 * It uses the UserRepository to talk to the database.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * We use constructor injection to get the things we need (our dependencies).
     * Spring will automatically provide instances of our repository and password encoder here.
     * @param userRepository The repository for user data.
     * @param passwordEncoder The tool we use to encrypt and check passwords.
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Gets all users from the database.
     * @return A list of all User objects.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Creates and saves a new user after checking if the username/email is already taken.
     * This method also encrypts the user's password before saving it.
     * @param user The User object with the raw password to save.
     * @return The saved User object, now with an ID and an encrypted password.
     * @throws IllegalStateException if the username or email is already in use.
     */
    public User createUser(User user) {
        // 1. Check if the username is already in use.
        userRepository.findByUsername(user.getUsername()).ifPresent(u -> {
            throw new IllegalStateException("Username is already taken");
        });

        // 2. Check if the email address is already in use.
        userRepository.findByEmail(user.getEmail()).ifPresent(u -> {
            throw new IllegalStateException("Email is already in use");
        });

        // 3. Encrypt the password before saving it to the database.
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 4. If everything is okay, save the new user.
        return userRepository.save(user);
    }

    /**
     * Authenticates a user based on their username and password.
     * @param username The user's username.
     * @param password The user's plain text password from the login form.
     * @return The User object if the password is correct.
     * @throws RuntimeException if the username doesn't exist or the password is wrong.
     */
    public User authenticate(String username, String password) {
        // Find the user by their username. If not found, throw an error.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Use the passwordEncoder to safely compare the plain text password
        // with the encrypted one stored in the database.
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return user;
    }
}