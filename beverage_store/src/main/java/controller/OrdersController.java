package beverage_store.controller;

import beverage_store.model.Order;
import beverage_store.model.User;
import beverage_store.repository.OrderRepository;
import beverage_store.repository.UserRepository;
import beverage_store.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/orders")
public class OrdersController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public OrdersController(OrderRepository orderRepository,
                            UserRepository userRepository,
                            UserService userService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // view list of all orders in database (admins) or the current user's orders
    @GetMapping
    public String viewListOfOrders(Model model) {
        log.info("User viewing list of orders");

        Authentication auth = userService.getCurrentUser();
        if (auth == null) {
            model.addAttribute("message", "Please log in");
            return "error";
        }

        boolean isAdmin = auth.getAuthorities() != null &&
                auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                        .anyMatch(a -> a.equals("ROLE_ADMIN"));

        if (isAdmin) {
            List<Order> orderList = orderRepository.findAll();
            model.addAttribute("orders", orderList);
            return "ordersList";
        }

        Optional<User> userOpt = userRepository.getUserWithEntitiesByUsername(auth.getName());
        if (userOpt.isPresent()) {
            List<Order> orderList = new ArrayList<>(userOpt.get().getOrders());
            model.addAttribute("orders", orderList);
            return "ordersList";
        }

        model.addAttribute("message", "No orders found for the current user");
        return "error";
    }

    @GetMapping("/{orderid}")
    public String viewSpecificOrder(@PathVariable("orderid") long orderID, Model model) {
        Optional<Order> selectedOrderOpt = orderRepository.findById(orderID);
        if (selectedOrderOpt.isEmpty()) {
            model.addAttribute("message", "The requested order doesn't exist");
            return "error";
        }
        Order selectedOrder = selectedOrderOpt.get();

        Authentication auth = userService.getCurrentUser();
        if (auth == null) {
            model.addAttribute("message", "Please log in");
            return "error";
        }

        boolean isAdmin = auth.getAuthorities() != null &&
                auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                        .anyMatch(a -> a.equals("ROLE_ADMIN"));

        String currentUsername = auth.getName();
        String orderOwner = selectedOrder.getCustomer() == null ? null : selectedOrder.getCustomer().getUsername();

        if (isAdmin || (orderOwner != null && orderOwner.equals(currentUsername))) {
            model.addAttribute("orderID", orderID);
            model.addAttribute("orderPrice", selectedOrder.getTotalPrice());
            model.addAttribute("orderCustomer", orderOwner);
            model.addAttribute("orderItems", selectedOrder.getItems());
            return "orderDetails";
        }

        model.addAttribute("message", "The requested order doesn't belong to the logged in user");
        return "error";
    }
}
