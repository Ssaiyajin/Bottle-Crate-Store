package beverage_store.service.impl;

import beverage_store.model.User;
import beverage_store.model.UserDTO;
import beverage_store.repository.UserRepository;
import beverage_store.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.AuthenticationException;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(11);

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User registerUser(User user) {
        if (!userRepository.existsByUsername(user.getUsername())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepository.save(user);
        }
        // return null to indicate registration failed (caller may handle)
        return null;
    }

    @Override
    public User updateUser(String userid, UserDTO userUpdateDTO) {
        return userRepository.findById(userid)
                .map(existingUser -> {
                    existingUser.setBillingaddresses(userUpdateDTO.getBillingAddressesAsSet());
                    existingUser.setDeliveryaddresses(userUpdateDTO.getDeliveryAddressesAsSet());
                    return userRepository.save(existingUser);
                })
                .orElse(null);
    }

    @Override
    public User getUser(String userid) {
        // prefer repository method that fetches related entities
        return userRepository.getUserWithEntitiesByUsername(userid).orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.getUserWithEntitiesByUsername(username);

        if (user.isPresent()) {
            return user.get();
        }

        throw new UsernameNotFoundException("User '" + username + "' not found!");
    }

    @Override
    public Authentication getCurrentUser() throws AuthenticationException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication;
        }
        // AuthenticationException is abstract, create an anonymous subclass for throwing
        throw new AuthenticationException("No user logged in") { };
    }
}
