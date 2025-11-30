package beverage_store.controller;

import jakarta.validation.Valid;

import beverage_store.model.Address;
import beverage_store.model.User;
import beverage_store.model.UserDTO;
import beverage_store.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import beverage_store.repository.UserRepository;

import java.util.Collections;

@Slf4j
@Controller
@RequestMapping(value = "/register")
public class RegistrationController {

    private final UserService userService;
    private final UserRepository userRepo;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public RegistrationController(UserRepository userRepo, UserService userService, AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping
    public String getRegistrationForm(Model model){

        log.info("Show registration page");
        UserDTO blankUser = new UserDTO();
        blankUser.setBillingAddresses(Collections.singletonList(new Address()));
        blankUser.setDeliveryAddresses(Collections.singletonList(new Address()));
        model.addAttribute("registrationForm", blankUser);

        return "register";
    }

    @PostMapping
    public String createUser(Model model, @Valid UserDTO registrationForm, Errors errors) {

        if(errors.hasErrors()) {
            log.info("User registration contained errors: {}", registrationForm);
            UserDTO blankUser = new UserDTO();
            blankUser.setBillingAddresses(Collections.singletonList(new Address()));
            blankUser.setDeliveryAddresses(Collections.singletonList(new Address()));
            model.addAttribute("registrationForm", blankUser);
            return "register";
        }
        User user = registrationForm.toUser();

        long existingUsers = userRepo.count();
        if(existingUsers == 0L) {
            user.setRole("ADMIN");
        } else {
            user.setRole("CUSTOMER");
        }

        user = userService.registerUser(user);
        if(user == null || user.getUsername() == null) {
            log.info("User registration failed for: {}", registrationForm);
            UserDTO blankUser = new UserDTO();
            blankUser.setBillingAddresses(Collections.singletonList(new Address()));
            blankUser.setDeliveryAddresses(Collections.singletonList(new Address()));
            model.addAttribute("registrationForm", blankUser);
            model.addAttribute("error", "This username already exists or registration failed");
            return "register";
        }
        log.info("New user created: {}", user.getUsername());

        String username = user.getUsername();
        String password = registrationForm.getPassword();
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        try {
            Authentication authenticatedUser = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
            return "redirect:/beverages";
        } catch (AuthenticationException ex) {
            log.warn("Auto-login after registration failed for {}: {}", username, ex.getMessage());
            model.addAttribute("message", "Registration succeeded but automatic login failed. Please log in manually.");
            return "login";
        }
    }
}
