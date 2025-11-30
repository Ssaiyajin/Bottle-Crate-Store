package beverage_store.controller;

import beverage_store.model.*;
import beverage_store.repository.OrderRepository;
import beverage_store.service.BeverageService;
import beverage_store.service.ShoppingCartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class CheckOutControllerTest {

    @MockBean
    private BeverageService beverageService;

    @MockBean
    private ShoppingCartService shoppingCartService;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private MockMvc mvc;

    private Order sampleOrder;
    private List<OrderItem> sampleItems = new ArrayList<>();
    private User sampleUser;
    private Long orderId;

    @BeforeEach
    public void initCommonUsedData() {
        // Sample User
        Address address = new Address(null, "An der Spinnerei", "13", "96049");
        Address address2 = new Address(null, "An der Test", "15", "96000");
        sampleUser = new User();
        sampleUser.setUsername("Max");
        sampleUser.setPassword("123456");
        sampleUser.setRole("CUSTOMER");
        sampleUser.setEmail("mail@example.de");
        sampleUser.setBirthday(LocalDate.of(1996, 8, 2));
        // fix duplicate setters
        sampleUser.setBillingaddresses(Collections.singleton(address2));
        sampleUser.setDeliveryaddresses(Collections.singleton(address));

        //Schlenkerla (Bottle)
        Bottle schlenkerla = new Bottle();
        schlenkerla.setId(1L);
        schlenkerla.setName("Schlenkerla");
        schlenkerla.setPic("https://www.getraenkewelt-weiser.de/images/product/01/85/40/18546-0-p.jpg");
        schlenkerla.setVolume(0.5);
        schlenkerla.setVolumePercent(5.1);
        schlenkerla.setPrice(0.89);
        schlenkerla.setSupplier("Rauchbierbrauerei Schlenkerla");
        schlenkerla.setInStock(438);

        //Crate Schlenkerla
        Crate crateSchlenkerla = new Crate();
        crateSchlenkerla.setId(2L);
        crateSchlenkerla.setName("20 Crate Schlenkerla");
        crateSchlenkerla.setPic("https://www.getraenkedienst.com/media/image/34/b1/39/Brauerei_Heller_Schlenkerla_Aecht_Schlenkerla_Rauchbier_Maerzen_20_x_0_5l.jpg");
        crateSchlenkerla.setNoOfBottles(20);
        crateSchlenkerla.setPrice(18.39);
        crateSchlenkerla.setInStock(13);
        crateSchlenkerla.setBottle(schlenkerla);

        this.sampleItems.clear();
        this.sampleItems.add(new OrderItem(schlenkerla, 145));
        this.sampleItems.add(new OrderItem(crateSchlenkerla, 6));

        this.sampleOrder = new Order();
        this.sampleOrder.addOrderItem(new OrderItem(schlenkerla, 145));
        this.sampleOrder.setCustomer(this.sampleUser);
        this.sampleOrder.setId(1L);
        this.sampleOrder.setPrice(this.sampleOrder.priceTotal(this.sampleOrder.getItems()));
    }

    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getFinalReviewOfSelectedItems_shouldSuccess() throws Exception {
        orderId = 1L;
        when(this.orderRepository.findById(orderId)).thenReturn(Optional.of(sampleOrder));

        this.mvc.perform(get("/shoppingcart/checkout/" + orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"))
                .andExpect(model().attribute("orderID", orderId))
                .andExpect(model().attribute("orderPrice", sampleOrder.getPrice()))
                .andExpect(model().attribute("orderCustomer", sampleOrder.getCustomer().getUsername()))
                .andExpect(model().attribute("orderItems", sampleOrder.getItems()))
                .andExpect(content().string(containsString(sampleOrder.getItems().get(0).getBeverage().getName())));

        verify(this.orderRepository, times(2)).findById(orderId);
    }

    @Test
    public void getFinalReviewOfSelectedItems_shouldFailWhenNotAuthenticated() throws Exception {
        this.mvc.perform(get("/shoppingcart/checkout/" + 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "Moritz", roles = "CUSTOMER")
    public void getFinalReviewOfSelectedItems_shouldFailWhenOrderDoesntBelongToUser() throws Exception {
        orderId = 1L;
        when(this.orderRepository.findById(orderId)).thenReturn(Optional.of(sampleOrder));

        this.mvc.perform(get("/shoppingcart/checkout/" + orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "The requested order doesn't belong the logged in user"))
                .andExpect(content().string(containsString("The requested order doesn&#39;t belong the logged in user")));

        verify(this.orderRepository, times(2)).findById(orderId);
    }

    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getFinalReviewOfSelectedItems_shouldFailWhenOrderDoesntExist() throws Exception {
        orderId = 1L;
        when(this.orderRepository.findById(orderId)).thenReturn(Optional.empty());

        this.mvc.perform(get("/shoppingcart/checkout/" + orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "The requested order does not exist."))
                .andExpect(content().string(containsString("The requested order does not exist.")));

        verify(this.orderRepository, times(1)).findById(orderId);
    }

    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void postCompletePurchase_shouldSuccess() throws Exception {
        // prepare order that controller will create
        when(this.shoppingCartService.getItemsInCart()).thenReturn(this.sampleItems);
        // capture the saved order and return it with id
        when(this.orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(42L);
            return o;
        });
        when(this.restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        this.mvc.perform(post("/shoppingcart/checkout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shoppingcart/checkout/42"));

        verify(this.shoppingCartService, times(1)).getItemsInCart();
        verify(this.orderRepository, times(1)).save(any(Order.class));
        // verify beverage quantity updates for each item
        for (OrderItem item : this.sampleItems) {
            verify(this.beverageService, times(1)).updateBeverageQuantity(item.getBeverage().getId(),
                    item.getBeverage().getInStock() - item.getQuantity());
        }
        verify(this.shoppingCartService, times(1)).clearAllItems();
        verify(this.restTemplate, atLeastOnce()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void postCompletePurchase_shouldFailWhenNotAuthenticated() throws Exception {
        this.mvc.perform(post("/shoppingcart/checkout"))
                .andExpect(status().isForbidden());
    }

}
