package beverage_store.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import beverage_store.model.*;
import beverage_store.repository.OrderRepository;
import beverage_store.service.BeverageService;
import beverage_store.service.ShoppingCartService;
import beverage_store.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping(value = "/shoppingcart/checkout")
public class CheckOutController {

    private final OrderRepository orderRepository;
    private final ShoppingCartService shoppingCartService;
    private final UserService userService;
    private final BeverageService beverageService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public CheckOutController(OrderRepository orderRepository,
                              ShoppingCartService shoppingCartService,
                              UserService userService,
                              BeverageService beverageService,
                              RestTemplate restTemplate,
                              ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.shoppingCartService = shoppingCartService;
        this.userService = userService;
        this.beverageService = beverageService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/{orderid}")
    public String successPurchaseSummary(@PathVariable Long orderid, Model model) {
        log.info("User finished purchase - show summary for order {}", orderid);

        Optional<Order> selectedOrderOpt = orderRepository.findById(orderid);
        if (selectedOrderOpt.isEmpty()) {
            model.addAttribute("message", "The requested order does not exist.");
            return "error";
        }
        Order selectedOrder = selectedOrderOpt.get();

        org.springframework.security.core.Authentication userAuth = userService.getCurrentUser();
        if (userAuth == null) {
            model.addAttribute("message", "Please log in.");
            return "error";
        }

        String currentUsername = userAuth.getName();
        String orderOwner = selectedOrder.getCustomer() == null ? null : selectedOrder.getCustomer().getUsername();

        if (!currentUsername.equals(orderOwner)) {
            model.addAttribute("message", "The requested order doesn't belong to the logged in user");
            return "error";
        }

        model.addAttribute("orderID", orderid);
        model.addAttribute("orderPrice", selectedOrder.getTotalPrice());
        model.addAttribute("orderCustomer", orderOwner);
        model.addAttribute("orderItems", selectedOrder.getItems());
        return "checkout";
    }

    @PostMapping
    public String completePurchase(Model model) {
        log.info("User confirmed the order");

        org.springframework.security.core.Authentication auth = userService.getCurrentUser();
        if (auth == null) {
            model.addAttribute("message", "Please log in.");
            return "error";
        }

        String username = auth.getName();
        User orderingUser = userService.getUser(username);
        if (orderingUser == null) {
            model.addAttribute("message", "User not found.");
            return "error";
        }

        List<OrderItem> listOfItems = shoppingCartService.getItemsInCart();
        if (listOfItems == null || listOfItems.isEmpty()) {
            model.addAttribute("message", "Shopping cart is empty.");
            return "error";
        }

        Order newOrder = new Order();
        newOrder.setCustomer(orderingUser);
        // Add items (addOrderItem will set order relationship and recalc item price)
        listOfItems.forEach(newOrder::addOrderItem);

        // Calculate total using BigDecimal helper
        java.math.BigDecimal totalPrice = newOrder.priceTotal(listOfItems);
        newOrder.setTotalPrice(totalPrice);

        // Persist order
        orderRepository.save(newOrder);

        // Update beverage stock quantities
        for (OrderItem oi : listOfItems) {
            if (oi == null || oi.getBeverage() == null || oi.getBeverage().getId() == null) continue;
            int orderedQuantity = oi.getQuantity();
            int beverageQuantity = oi.getBeverage().getInStock();
            Long beverageID = oi.getBeverage().getId();
            beverageService.updateBeverageQuantity(beverageID, beverageQuantity - orderedQuantity);
        }

        // Build DTO for PDF generation
        OrderDTO orderDTO = new OrderDTO();
        List<OrderListDTO> orderListDTO = new ArrayList<>();
        for (OrderItem oi : listOfItems) {
            orderListDTO.add(new OrderListDTO(
                    oi.getPrice().doubleValue(),
                    oi.getQuantity(),
                    oi.getBeverage() == null ? null : oi.getBeverage().getName(),
                    oi.getBeverage() == null ? null : oi.getBeverage().getPic(),
                    oi.getBeverage() == null ? null : oi.getBeverage().getId()
            ));
        }
        orderDTO.setId(newOrder.getId());
        orderDTO.setTotalPrice(totalPrice);
        orderDTO.setListOfItems(orderListDTO);
        orderDTO.setUserEmail(orderingUser.getEmail());

        // Safely get postal code from delivery addresses (guard nulls)
        String postalCode = null;
        // field in User is `deliveryaddresses` so use corresponding getter
        if (orderingUser.getDeliveryaddresses() != null && !orderingUser.getDeliveryaddresses().isEmpty()) {
            Address addr = orderingUser.getDeliveryaddresses().iterator().next();
            if (addr != null) postalCode = addr.getPostalCode();
        }
        orderDTO.setPostalCode(postalCode);

        orderDTO.setTimestamp(Instant.now().toString());

        // Call PDF generator function (external)
        String endPoint = "http://localhost:8081/";
        try {
            String orderJson = objectMapper.writeValueAsString(orderDTO);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(orderJson, headers);
            ResponseEntity<String> response = restTemplate.exchange(endPoint, HttpMethod.POST, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("pdf Function invoked successfully from CheckOut Controller");
            } else {
                log.warn("pdf Function invocation returned status {}", response.getStatusCode());
            }
        } catch (JsonProcessingException | RestClientException e) {
            log.warn("Failed to invoke pdf generator: {}", e.getMessage());
        }

        // Clear cart and redirect to summary
        shoppingCartService.clearAllItems();
        log.info("shopping cart cleared");
        return "redirect:/shoppingcart/checkout/" + newOrder.getId();
    }
}


