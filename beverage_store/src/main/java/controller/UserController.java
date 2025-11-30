package beverage_store.controller;

import beverage_store.model.User;
import beverage_store.model.UserDTO;
import beverage_store.repository.UserRepository;
import beverage_store.service.ShoppingCartService;
import beverage_store.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
//This controller is used to work with User data
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ShoppingCartService shoppingCartService;

    public UserController(UserRepository userRepository,
                          UserService userService,
                          ShoppingCartService shoppingCartService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.shoppingCartService = shoppingCartService;
    }

    private boolean isAuthorized(Authentication auth, String userId) {
        return auth != null && (auth.getName().equals(userId)
                || auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    private String loadUsersIntoModel(Model model, String userId) {
        List<String> ids = Collections.singletonList(userId);
        List<User> usersinfo = this.userRepository.findAllById(ids);
        model.addAttribute("usersinfo", usersinfo);
        return "userInfo";
    }

    @GetMapping("/usersinfo")
    @PreAuthorize("isAuthenticated()")
    public String getUsers(Model model, @RequestParam String user_id) {
        log.info("Show user info for {}", user_id);
        Authentication userauth = userService.getCurrentUser();
        if (!isAuthorized(userauth, user_id)) {
            model.addAttribute("message", "The requested user isn't the logged in user");
            return "error";
        }
        return loadUsersIntoModel(model, user_id);
    }

    @GetMapping("/usersinfo/address")
    @PreAuthorize("isAuthenticated()")
    public String getUsersAddress(Model model) {
        log.info("Show user address");
        Authentication userauth = userService.getCurrentUser();
        if (userauth == null) {
            model.addAttribute("message", "Please log in.");
            return "error";
        }
        return loadUsersIntoModel(model, userauth.getName());
    }

    @PostMapping("/usersinfo/updateaddress/{user_id}")
    @PreAuthorize("isAuthenticated()")
    public String updateAddress(Model model, @PathVariable(value = "user_id") String user_id, UserDTO userdto) {
        Authentication user = userService.getCurrentUser();
        if (!isAuthorized(user, user_id)) {
            model.addAttribute("message", "The requested user isn't the logged in user");
            return "error";
        }
        userService.updateUser(user_id, userdto);
        return "redirect:/usersinfo?user_id=" + user_id;
    }

    @GetMapping("/userProfile")
    @PreAuthorize("isAuthenticated()")
    public String getUserProfile(Model model) {
        log.info("Show User Profile");
        Authentication auth = userService.getCurrentUser();
        if (auth == null) {
            model.addAttribute("message", "Please log in.");
            return "error";
        }
        List<User> users = userRepository.findAllById(Collections.singletonList(auth.getName()));
        model.addAttribute("userprofile", users.isEmpty() ? null : users.get(0));
        return "userProfile";
    }

    @PostMapping("/usersinfo/updateProfile/{user_id}")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(Model model, @PathVariable(value = "user_id") String user_id, UserDTO userdto) {
        Authentication user = userService.getCurrentUser();
        if (!isAuthorized(user, user_id)) {
            model.addAttribute("message", "The requested user isn't the logged in user");
            return "error";
        }
        userService.updateUser(user_id, userdto);
        log.info("Profile updated successfully for {}", user_id);
        return "redirect:/userProfile";
    }
}
