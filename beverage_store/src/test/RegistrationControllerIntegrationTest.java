package beverage_store;

import com.fasterxml.jackson.databind.ObjectMapper;
import beverage_store.model.Address;
import beverage_store.model.UserDTO;
import beverage_store.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class RegistrationControllerIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    private UserDTO firstRegistration;
    private UserDTO secondRegistration;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeEach
    public void initCommonUsedData() {
        Address address = new Address(null, "An der Spinnerei", "13", "96049");
        Address address2 = new Address(null, "An der Test", "15", "96000");

        firstRegistration = new UserDTO("Password123", LocalDate.of(1996, 8, 2), "mail@mail.de", "Admin", Collections.singletonList(address), Collections.singletonList(address2));
        secondRegistration = new UserDTO("Password123", LocalDate.of(1995, 2, 12), "example@mail.de", "Max", Collections.singletonList(address), Collections.singletonList(address2));
    }

    @Test
    @Transactional
    public void postRegistrationRequest_createAdminUserAsFirstUser() throws Exception {
        // test storing admin user
        this.mvc.perform(post("/register")
                .params(convert(firstRegistration))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/beverages"));

        //confirm that stored user has role admin
        assertTrue(this.userService.loadUserByUsername("Admin")
                .getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }


    @Test
    @Transactional
    public void postRegistrationRequest_createCustomerUserAsNotFirstUser() throws Exception {
        // given
        this.mvc.perform(post("/register")
                .params(convert(firstRegistration))
                .with(csrf()));

        //when
        this.mvc.perform(post("/register")
                .params(convert(secondRegistration))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/beverages"));

        //confirm that stored user has role customer
        assertTrue(this.userService.loadUserByUsername("Max")
                .getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }


    private static MultiValueMap<String, String> convert(UserDTO dto) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        addAddressParameters(parameters, "billingAddresses", dto.getBillingAddresses());
        addAddressParameters(parameters, "deliveryAddresses", dto.getDeliveryAddresses());

        parameters.add("password", dto.getPassword());
        if (dto.getBirthday() != null) {
            parameters.add("birthday", dto.getBirthday().toString());
        }
        parameters.add("email", dto.getEmail());
        parameters.add("username", dto.getUsername());
        return parameters;
    }

    private static void addAddressParameters(MultiValueMap<String, String> parameters, String baseName, List<Address> addresses) {
        if (addresses == null) return;

        for (int i = 0; i < addresses.size(); i++) {
            Address a = addresses.get(i);
            if (a == null) continue;

            @SuppressWarnings("unchecked")
            Map<String, Object> maps = OBJECT_MAPPER.convertValue(a, HashMap.class);
            for (Map.Entry<String, Object> entry : maps.entrySet()) {
                if (entry.getValue() == null) continue;
                String key = baseName + "[" + i + "]." + entry.getKey();
                parameters.add(key, entry.getValue().toString());
            }
        }
    }

}
