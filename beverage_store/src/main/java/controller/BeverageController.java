package beverage_store.controller;

import beverage_store.model.Beverage;
import beverage_store.model.Bottle;
import beverage_store.model.Crate;
import beverage_store.model.OrderItemDTO;
import beverage_store.repository.BeverageRepository;
import beverage_store.repository.BottleRepository;
import beverage_store.repository.CrateRepository;
import beverage_store.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping({"/", "/beverages"})
public class BeverageController {

    private final ShoppingCartService shoppingCartService;
    private final BeverageRepository beverageRepository;
    private final BottleRepository bottleRepository;
    private final CrateRepository crateRepository;

    public BeverageController(ShoppingCartService shoppingCartService,
                              BeverageRepository beverageRepository,
                              BottleRepository bottleRepository,
                              CrateRepository crateRepository) {
        this.shoppingCartService = shoppingCartService;
        this.beverageRepository = beverageRepository;
        this.bottleRepository = bottleRepository;
        this.crateRepository = crateRepository;
    }

    private static final Comparator<Beverage> BEVERAGE_ID_COMPARATOR =
            Comparator.comparing(Beverage::getId, Comparator.nullsLast(Long::compareTo));

    @GetMapping
    public String home(Model model) {

        log.info("** Client requested all beverages");
        List<Beverage> beverages = this.beverageRepository.findAll();
        beverages.sort(BEVERAGE_ID_COMPARATOR);

        model.addAttribute("beverages", beverages);
        model.addAttribute("item", new OrderItemDTO());
        model.addAttribute("listofitems", shoppingCartService.getItemsInCart() == null
                ? 0 : shoppingCartService.getItemsInCart().size());
        return "beverages";
    }

    @GetMapping("/addnewbottle")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String addNewBottle(Model model) {
        log.info("** Client wants to add new bottle");
        model.addAttribute("bottle", new Bottle());
        return "addBottle";
    }

    @PostMapping("/addnewbottle")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String addBottle(@Valid Bottle newBottle, Errors errors, Model model) {
        log.info("** Client added a new Bottle: {}", newBottle);
        if (errors.hasErrors()) {
            log.info("...but there are errors : {}", newBottle);
            model.addAttribute("bottle", newBottle);
            return "addBottle";
        }

        Double vp = newBottle.getVolumePercent();
        newBottle.setAlcoholic(Boolean.valueOf(vp != null && vp > 0.0));

        this.bottleRepository.save(newBottle);
        return "redirect:/beverages";
    }

    @GetMapping("/addnewcrate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String addNewCrate(Model model) {
        log.info("** Client wants to add new crate");
        model.addAttribute("crate", new Crate());
        model.addAttribute("bottles", bottleRepository.findAll());
        return "addCrate";
    }

    @PostMapping("/addnewcrate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String addCrate(@Valid Crate newCrate, Errors errors, Model model) {
        log.info("** Client added a new Crate: {}", newCrate);
        if (errors.hasErrors()) {
            log.info("...but there are errors : {}", newCrate);
            model.addAttribute("crate", newCrate);
            model.addAttribute("bottles", bottleRepository.findAll());
            return "addCrate";
        }
        this.crateRepository.save(newCrate);
        return "redirect:/beverages";
    }
}
