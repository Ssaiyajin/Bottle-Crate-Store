package beverage_store.controller;


import beverage_store.model.*;
import beverage_store.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.thymeleaf.expression.Numbers;


import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest//(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class OrdersControllerTest {

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mvc;

    private Numbers numbers = new Numbers(Locale.GERMANY);

    private List<Order> allOrders = new ArrayList<>();
    private List<Order> maxOrders = new ArrayList<>();
    private User max;


    @BeforeEach
    public void initCommonUsedData() {
        // Max User
        Address address = new Address(null, "An der Spinnerei", "13", "96049");
        Address address2 = new Address(null, "An der Test", "15", "96000");
        max = new User();
        max.setUsername("Max");
        max.setPassword("123456");
        max.setRole("CUSTOMER");
        max.setBirthday(LocalDate.of(1996, 8, 2));
        max.setBillingaddresses(Collections.singleton(address2));
        max.setDeliveryaddresses(Collections.singleton(address));

        // Other User
        User other = new User();
        other.setUsername("Moritz");
        other.setPassword("123456");
        other.setRole("CUSTOMER");
        other.setBirthday(LocalDate.of(1995, 12, 2));
        other.setBillingaddresses(Collections.singleton(address2));
        other.setDeliveryaddresses(Collections.singleton(address));

        //Schlenkerla
        Bottle schlenkerla = new Bottle();
        schlenkerla.setName("Schlenkerla");
        schlenkerla.setPic("https://www.getraenkewelt-weiser.de/images/product/01/85/40/18546-0-p.jpg");
        schlenkerla.setVolume(0.5);
        schlenkerla.setVolumePercent(5.1);
        schlenkerla.setPrice(0.89);
        schlenkerla.setSupplier("Rauchbierbrauerei Schlenkerla");
        schlenkerla.setInStock(438);

        //Crate Schlenkerla
        Crate crateSchlenkerla = new Crate();
        crateSchlenkerla.setName("20 Crate Schlenkerla");
        crateSchlenkerla.setPic("https://www.getraenkedienst.com/media/image/34/b1/39/Brauerei_Heller_Schlenkerla_Aecht_Schlenkerla_Rauchbier_Maerzen_20_x_0_5l.jpg");
        crateSchlenkerla.setNoOfBottles(20);
        crateSchlenkerla.setPrice(18.39);
        crateSchlenkerla.setInStock(13);
        crateSchlenkerla.setBottle(schlenkerla);

        Order maxOrder = new Order();
        maxOrder.addOrderItem(new OrderItem(crateSchlenkerla,145));
        maxOrder.setCustomer(this.max);
        maxOrder.setPrice(maxOrder.priceTotal(maxOrder.getItems()));
        maxOrder.setId(1L);


        Order moritzOrder = new Order();
        moritzOrder.addOrderItem(new OrderItem(schlenkerla,145));
        moritzOrder.setCustomer(other);
        moritzOrder.setPrice(moritzOrder.priceTotal(moritzOrder.getItems()));
        moritzOrder.setId(2L);

        maxOrders.add(maxOrder);

        allOrders.add(maxOrder);
        allOrders.add(moritzOrder);

        this.max.setOrders(maxOrders);
    }


    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getViewListOfOrders_shouldSuccessForUser() throws Exception {
        when(this.userRepository.getUserWithEntitiesByUsername("Max")).thenReturn(Optional.ofNullable(this.max));

        this.mvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("ordersList"))
                .andExpect(model().attribute("orders", maxOrders))
                .andExpect(content().string(containsString(String.valueOf(numbers.formatDecimal(this.maxOrders.get(0).getPrice(), 0, "COMMA", 2 , "POINT")))));

        verify(this.userRepository, times(1)).getUserWithEntitiesByUsername("Max");
        assert (maxOrders.stream().allMatch((order -> order.getCustomer().getUsername().equals("Max"))));
    }


    @Test
    @WithMockUser(username = "Admin", roles = "ADMIN")
    public void getViewListOfOrders_shouldSuccessForAdmin() throws Exception {
        when(this.orderRepository.findAll()).thenReturn(this.allOrders);


        this.mvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("ordersList"))
                .andExpect(model().attribute("orders", allOrders))
                .andExpect(content().string(containsString(String.valueOf(numbers.formatDecimal(this.allOrders.get(0).getPrice(), 0, "COMMA", 2 , "POINT")))));

        verify(this.orderRepository, times(1)).findAll();
    }


    @Test
    public void getViewListOfOrders_shouldFailWhenNotAuthenticated() throws Exception {
        this.mvc.perform(get("/orders"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }


    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getViewSpecificOrder_shouldSuccess() throws Exception {
        Long orderId = this.maxOrders.get(0).getId();
        Order order = this.maxOrders.get(0);

        when(this.orderRepository.findById(orderId)).thenReturn(Optional.ofNullable(this.maxOrders.get(0)));

        this.mvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("orderDetails"))
                .andExpect(model().attribute("orderID", orderId))
                .andExpect(model().attribute("orderPrice", order.getPrice()))
                .andExpect(model().attribute("orderCustomer", order.getCustomer().getUsername()))
                .andExpect(model().attribute("orderItems", order.getItems()))
                .andExpect(content().string(containsString(String.valueOf(this.maxOrders.get(0).getItems().get(0).getBeverage().getName()))));

        verify(this.orderRepository, times(1)).findById(orderId);
        assert (maxOrders.get(0).getCustomer().getUsername().equals("Max"));
    }

    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getViewSpecificOrder_shouldFailWhenOrderDoesntBelongToUser() throws Exception {
        Long orderId = allOrders.get(1).getId();

        when(this.orderRepository.findById(orderId)).thenReturn(Optional.ofNullable(this.allOrders.get(1)));

        this.mvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "The requested order doesn't belong the logged in user"))
                .andExpect(content().string(containsString("The requested order doesn&#39;t belong the logged in user")));

        verify(this.orderRepository, times(1)).findById(orderId);
        assert (!allOrders.get(1).getCustomer().getUsername().equals("Max"));
    }

    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getViewSpecificOrder_shouldFailWhenOrderDoesntExist() throws Exception {
        Long orderId = 3L;

        // stub explicit empty result to avoid NullPointer from mock
        when(this.orderRepository.findById(orderId)).thenReturn(Optional.empty());

        this.mvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "The requested order doesn't exist"))
                .andExpect(content().string(containsString("The requested order doesn&#39;t exist")));

        verify(this.orderRepository, times(1)).findById(orderId);
    }

    @Test
    public void getViewSpecificOrder_shouldFailWhenNotAuthenticated() throws Exception {
        Long orderId = 1L;
        this.mvc.perform(get("/orders/" + orderId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

}
