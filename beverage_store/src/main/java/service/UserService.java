package beverage_store.service;

import beverage_store.model.User;
import beverage_store.model.UserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Service API for user operations.
 */
public interface UserService {

    /**
     * Register a new user.
     */
    User registerUser(User user);

    /**
     * Update an existing user identified by userid using the provided DTO.
     */
    User updateUser(String userid, UserDTO userUpdateDTO);

    /**
     * Retrieve a user by id/username.
     */
    User getUser(String userid);

    /**
     * Load user details for authentication.
     */
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    /**
     * Return the currently authenticated principal information.
     */
    Authentication getCurrentUser() throws AuthenticationException;
}
