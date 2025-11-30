package beverage_store.controller;

import beverage_store.model.Beverage;
import beverage_store.model.OrderItem;
import beverage_store.model.OrderItemDTO;
import beverage_store.repository.BeverageRepository;
import beverage_store.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.Optional;

@RequestMapping("/shoppingcart")
@Slf4j
@Controller
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;
    private final BeverageRepository beverageRepository;

    @Autowired
    public ShoppingCartController(ShoppingCartService shoppingCartService,
                                  BeverageRepository beverageRepository) {
        this.shoppingCartService = shoppingCartService;
        this.beverageRepository = beverageRepository;
    }

    //Show items in the cart
    @GetMapping
    public ModelAndView shoppingCart() {
        ModelAndView modelAndview = new ModelAndView("reviewCart");
        log.info("Review Shopping Cart");
        modelAndview.addObject("update", new OrderItemDTO());
        modelAndview.addObject("items", shoppingCartService.getItemsInCart());
        modelAndview.addObject("total", shoppingCartService.getTotal());
        return modelAndview;
    }

    //Add OrderItem to the Cart
    @PostMapping("/add")
    public String addItem(@Valid OrderItemDTO item, BindingResult bindingResult, RedirectAttributes ra) {
        log.info("Add OrderItem to the Cart");
        if (bindingResult.hasErrors()) {
            log.warn("Invalid OrderItemDTO on add: {}", bindingResult.getAllErrors());
            ra.addFlashAttribute("error", "Invalid item data");
            return "redirect:/beverages";
        }

        Optional<Beverage> beverage = beverageRepository.findById(item.getBeverageId());
        if (beverage.isEmpty()) {
            log.warn("Tried to add unknown beverage id {}", item.getBeverageId());
            ra.addFlashAttribute("error", "Beverage not found");
            return "redirect:/beverages";
        }

        shoppingCartService.addItem(new OrderItem(beverage.get(), item.getQuantity()));
        return "redirect:/beverages";
    }

    //Update OrderItem in cart
    @PostMapping("/update")
    public String updateItem(@Valid OrderItemDTO item, BindingResult bindingResult, RedirectAttributes ra) {
        log.info("Update OrderItem in cart");
        if (bindingResult.hasErrors()) {
            log.warn("Invalid OrderItemDTO on update: {}", bindingResult.getAllErrors());
            ra.addFlashAttribute("error", "Invalid update data");
            return "redirect:/shoppingcart";
        }

        Optional<Beverage> beverage = beverageRepository.findById(item.getBeverageId());
        if (beverage.isEmpty()) {
            log.warn("Tried to update unknown beverage id {}", item.getBeverageId());
            ra.addFlashAttribute("error", "Beverage not found");
            return "redirect:/shoppingcart";
        }

        shoppingCartService.updateItem(new OrderItem(beverage.get(), item.getQuantity()));
        return "redirect:/shoppingcart";
    }

    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id, RedirectAttributes ra) {
        log.info("Deleting OrderItem from cart (beverage id {})", id);

        Optional<Beverage> beverage = beverageRepository.findById(id);
        if (beverage.isEmpty()) {
            log.warn("Tried to delete unknown beverage id {}", id);
            ra.addFlashAttribute("error", "Beverage not found");
            return "redirect:/shoppingcart";
        }

        // Use removeItem(OrderItem) - implemented in ShoppingCartService to remove by beverage id
        shoppingCartService.removeItem(new OrderItem(beverage.get(), 1));
        ra.addFlashAttribute("message", "Item removed from cart");
        return "redirect:/shoppingcart";
    }
}
