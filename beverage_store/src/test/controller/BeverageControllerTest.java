package beverage_store.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import beverage_store.model.*;
import beverage_store.repository.BeverageRepository;
import beverage_store.repository.BottleRepository;
import beverage_store.repository.CrateRepository;
import beverage_store.service.ShoppingCartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
public class BeverageControllerTest {

    @MockBean
    private BeverageRepository beverageRepository;

    @MockBean
    private ShoppingCartService shoppingCartService;

    @MockBean
    private BottleRepository bottleRepository;

    @MockBean
    private CrateRepository crateRepository;

    @Autowired
    private MockMvc mvc;


    private List<Beverage> beverages = new ArrayList<>();
    private List<OrderItem> orderItems = new ArrayList<>();
    private Bottle exampleBottle;
    private Crate exampleCrate;


    @BeforeEach
    public void initCommonUsedData() {
        //Schlenkerla
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

        this.exampleBottle = schlenkerla;
        this.exampleCrate = crateSchlenkerla;
        this.beverages.clear();
        this.beverages.add(schlenkerla);
        this.beverages.add(crateSchlenkerla);
        this.orderItems.clear();
    }


    @Test
    public void getHome_shouldSuccess() throws Exception {
        when(this.beverageRepository.findAll()).thenReturn(beverages);
        when(this.shoppingCartService.getItemsInCart()).thenReturn(orderItems);

        this.mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("beverages"))
                .andExpect(model().attribute("beverages", beverages))
                .andExpect(model().attribute("listofitems", orderItems.size()))
                .andExpect(content().string(containsString(this.beverages.get(0).getName())));

        verify(this.beverageRepository, times(1)).findAll();
        verify(this.shoppingCartService, times(1)).getItemsInCart();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAddNewBottleForm_shouldSuccess() throws Exception {

        this.mvc.perform(get("/addnewbottle"))
                .andExpect(status().isOk())
                .andExpect(view().name("addBottle"))
                .andExpect(model().attributeExists("bottle"))
                .andExpect(content().string(containsString("Add New Bottle")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void getAddNewBottleForm_shouldFailOnWrongRole() throws Exception {

        this.mvc.perform(get("/addnewbottle"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void postAddBottle_shouldSuccess() throws Exception {
        // controller will bind params to a new Bottle; capture saved instance
        this.mvc.perform(
                post("/addnewbottle")
                    .params(convert(this.exampleBottle))
                    .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("beverages"));

        ArgumentCaptor<Bottle> captor = ArgumentCaptor.forClass(Bottle.class);
        verify(this.bottleRepository, times(1)).save(captor.capture());
        Bottle saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(this.exampleBottle.getName(), saved.getName());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void postAddBottle_shouldFailOnInvalidObject() throws Exception {
        Bottle invalid = new Bottle();
        invalid.setName(""); // invalid name

        this.mvc.perform(
                post("/addnewbottle")
                    .params(convert(invalid))
                    .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("bottle"))
                .andExpect(view().name("addBottle"));

        verify(this.bottleRepository, times(0)).save(any(Bottle.class));
    }


    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void postAddBottle_shouldFailOnWrongRole() throws Exception {

        this.mvc.perform(
                post("/addnewbottle")
                        .params(convert(this.exampleBottle))
                        .with(csrf())
                ).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAddNewCrateForm_shouldSuccess() throws Exception {
        Crate crate = new Crate();
        crate.setBottle(new Bottle());

        when(this.bottleRepository.findAll()).thenReturn(Collections.singletonList(this.exampleBottle));

        this.mvc.perform(get("/addnewcrate"))
                .andExpect(status().isOk())
                .andExpect(view().name("addCrate"))
                .andExpect(model().attributeExists("crate"))
                .andExpect(model().attribute("bottles", Collections.singletonList(this.exampleBottle)))
                .andExpect(content().string(containsString("Add New Crate")))
                .andExpect(content().string(containsString(this.exampleBottle.getName())));

        verify(this.bottleRepository, times(1)).findAll();
    }


    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void getAddNewCrateForm_shouldFailOnWrongRole() throws Exception {

        this.mvc.perform(get("/addnewcrate"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void postAddCrate_shouldSuccess() throws Exception {
        this.mvc.perform(
                post("/addnewcrate")
                    .params(convert(this.exampleCrate))
                    .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("beverages"));

        ArgumentCaptor<Crate> captor = ArgumentCaptor.forClass(Crate.class);
        verify(this.crateRepository, times(1)).save(captor.capture());
        Crate saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(this.exampleCrate.getName(), saved.getName());
        assertNotNull(saved.getBottle());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void postAddCrate_shouldFailOnInvalidObject() throws Exception {
        Crate invalid = new Crate();
        invalid.setName("");
        invalid.setBottle(this.exampleBottle);

        when(this.bottleRepository.findAll()).thenReturn(Collections.singletonList(this.exampleBottle));

        this.mvc.perform(
                post("/addnewcrate")
                    .params(convert(invalid))
                    .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("crate"))
                .andExpect(model().attribute("bottles", Collections.singletonList(this.exampleBottle)))
                .andExpect(view().name("addCrate"));

        verify(this.crateRepository, times(0)).save(any(Crate.class));
        verify(this.bottleRepository, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void postAddCrate_shouldFailOnWrongRole() throws Exception {

        this.mvc.perform(
                post("/addnewcrate")
                        .params(convert(this.exampleCrate))
                        .with(csrf())
                ).andExpect(status().isForbidden());
    }

    private static MultiValueMap<String, String> convert(Object obj) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        ObjectMapper mapper = new ObjectMapper();

        if (obj instanceof Crate) {
            Crate crate = (Crate) obj;
            // convert crate fields (excluding bottle)
            Map<String, Object> crateMap = mapper.convertValue(crate, new TypeReference<Map<String, Object>>() {});
            crateMap.remove("bottle");
            crateMap.forEach((k,v) -> {
                if (v != null) parameters.add(k, String.valueOf(v));
            });
            // convert nested bottle fields as bottle.<field>
            Bottle b = crate.getBottle();
            if (b != null) {
                Map<String, Object> bottleMap = mapper.convertValue(b, new TypeReference<Map<String, Object>>() {});
                bottleMap.forEach((k,v) -> {
                    if (v != null) parameters.add("bottle." + k, String.valueOf(v));
                });
            }
        } else {
            Map<String, Object> map = mapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
            map.forEach((k,v) -> {
                if (v != null) parameters.add(k, String.valueOf(v));
            });
        }

        return parameters;
    }
}
