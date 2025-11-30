package beverage_store.service;

import beverage_store.model.Address;
import beverage_store.model.User;
import beverage_store.model.UserDTO;
import beverage_store.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User max;
    private User updatedUser;
    private UserDTO dto;
    private Address address;
    private Address address2;

    @BeforeEach
    public void initCommonUsedData() {
        address = new Address(null, "An der Spinnerei", "13", "96049");
        address2 = new Address(null, "An der Test", "15", "96000");

        max = new User();
        max.setUsername("Max");
        max.setPassword("123456");
        max.setRole("CUSTOMER");
        max.setBirthday(LocalDate.of(1996, 8, 2));
        max.setBillingaddresses(Collections.singleton(address2));
        max.setDeliveryaddresses(Collections.singleton(address));

        updatedUser = new User();
        updatedUser.setUsername("Max");
        updatedUser.setPassword("123456");
        updatedUser.setRole("CUSTOMER");
        updatedUser.setBirthday(LocalDate.of(1996, 8, 2));
        updatedUser.setBillingaddresses(Collections.singleton(address));
        updatedUser.setDeliveryaddresses(Collections.singleton(address2));

        dto = new UserDTO("password", LocalDate.now(), "mail@mail.de", "Username",
                Collections.singletonList(address2), Collections.singletonList(address));
    }

    @Test
    public void registerUser_ShouldSuccess() {
        when(userRepository.existsByUsername(max.getUsername())).thenReturn(false);
        when(userRepository.save(max)).thenReturn(max);

        User newUser = userService.registerUser(max);

        verify(userRepository, times(1)).existsByUsername(max.getUsername());
        verify(userRepository, times(1)).save(max);
        assertEquals(max, newUser);
    }

    @Test
    public void registerUser_ShouldFailWhenUserAlreadyExists() {
        when(userRepository.existsByUsername(max.getUsername())).thenReturn(true);

        User newUser = userService.registerUser(max);

        verify(userRepository, times(1)).existsByUsername(max.getUsername());
        assertEquals(new User(), newUser);
    }

    @Test
    public void registerUser_ShouldHandleNullUsernameGracefully() {
        User user = new User();
        user.setUsername(null);
        user.setPassword("pw");

        when(userRepository.existsByUsername(null)).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.registerUser(user);

        verify(userRepository, times(1)).existsByUsername(null);
        verify(userRepository, times(1)).save(user);
        assertEquals(user, result);
    }

    @Test
    public void registerUser_ShouldPropagateSaveExceptions() {
        when(userRepository.existsByUsername(max.getUsername())).thenReturn(false);
        when(userRepository.save(max)).thenThrow(new RuntimeException("save failed"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerUser(max));
        assertEquals("save failed", ex.getMessage());
        verify(userRepository).existsByUsername(max.getUsername());
        verify(userRepository).save(max);
    }

    @Test
    public void updateUser_ShouldSuccess() {
        when(userRepository.findById(max.getUsername())).thenReturn(Optional.of(max));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User user = userService.updateUser(max.getUsername(), dto);

        verify(userRepository, times(2)).findById(max.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
        assertNotNull(user);
        assertEquals(updatedUser.getBillingaddresses(), user.getBillingaddresses());
    }

    @Test
    public void updateUser_ShouldFailWhenUserDoesntExist() {
        when(userRepository.findById("May")).thenReturn(Optional.empty());

        User user = userService.updateUser("May", dto);

        verify(userRepository, times(1)).findById("May");
        assertNull(user);
    }

    @Test
    public void updateUser_ShouldNotOverwritePasswordWhenDtoPasswordNull() {
        when(userRepository.findById(max.getUsername())).thenReturn(Optional.of(max));
        UserDTO partialDto = new UserDTO(null, dto.getBirthday(), dto.getEmail(), dto.getUsername(), dto.getBillingaddresses(), dto.getDeliveryaddresses());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(max.getUsername(), partialDto);

        verify(userRepository, atLeastOnce()).findById(max.getUsername());
        verify(userRepository).save(any(User.class));
        assertEquals("123456", result.getPassword(), "Password should remain unchanged when DTO password is null");
    }

    @Test
    public void updateUser_ShouldPreserveAddressesWhenDtoAddressesNull() {
        when(userRepository.findById(max.getUsername())).thenReturn(Optional.of(max));
        UserDTO partialDto = new UserDTO("newpw", dto.getBirthday(), dto.getEmail(), dto.getUsername(), null, null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(max.getUsername(), partialDto);

        verify(userRepository, atLeastOnce()).findById(max.getUsername());
        verify(userRepository).save(any(User.class));
        assertEquals(max.getBillingaddresses(), result.getBillingaddresses(), "Billing addresses should remain unchanged when DTO billingaddresses is null");
        assertEquals(max.getDeliveryaddresses(), result.getDeliveryaddresses(), "Delivery addresses should remain unchanged when DTO deliveryaddresses is null");
    }

    @Test
    public void getUser_ShouldSuccess() {
        when(userRepository.findById(max.getUsername())).thenReturn(Optional.of(max));
        when(userRepository.getUserWithEntitiesByUsername(max.getUsername())).thenReturn(Optional.of(max));

        User user = userService.getUser(max.getUsername());

        verify(userRepository, times(1)).findById(max.getUsername());
        verify(userRepository, times(1)).getUserWithEntitiesByUsername(max.getUsername());
        assertEquals(max, user);
    }

    @Test
    public void getUser_ShouldFailWhenUserDoesntExist() {
        when(userRepository.findById("May")).thenReturn(Optional.empty());

        User user = userService.getUser("May");

        verify(userRepository, times(1)).findById("May");
        assertNull(user);
    }

    @Test
    public void loadUserByUsername_ShouldSuccess() {
        when(userRepository.getUserWithEntitiesByUsername(max.getUsername())).thenReturn(Optional.of(max));

        UserDetails details = userService.loadUserByUsername(max.getUsername());

        verify(userRepository, times(1)).getUserWithEntitiesByUsername(max.getUsername());
        assertEquals(max, details);
    }

    @Test
    public void loadUserByUsername_ShouldThrowUsernameNotFoundExceptionIfUserDoesntExists() {
        when(userRepository.getUserWithEntitiesByUsername("May")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("May"));

        verify(userRepository, times(1)).getUserWithEntitiesByUsername("May");
        assertEquals("User 'May' not found!", exception.getMessage());
    }

    @Test
    public void loadUserByUsername_ShouldPropagateRepositoryExceptions() {
        when(userRepository.getUserWithEntitiesByUsername("Boom")).thenThrow(new RuntimeException("db down"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.loadUserByUsername("Boom"));

        assertEquals("db down", ex.getMessage());
        verify(userRepository, times(1)).getUserWithEntitiesByUsername("Boom");
    }

    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getCurrentUser_ShouldSuccess() {
        Authentication auth = userService.getCurrentUser();
        assertEquals("Max", auth.getName());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
    }

    @Test
    public void getCurrentUser_ShouldThrowAuthenticationExceptionOnAnonymousUser() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getCurrentUser());
        assertEquals("No user logged in", exception.getMessage());
    }

    @Test
    @WithMockUser(username = "Admin", roles = {"ADMIN", "CUSTOMER"})
    public void getCurrentUser_AdminHasMultipleRoles() {
        Authentication auth = userService.getCurrentUser();
        assertEquals("Admin", auth.getName());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
    }
}
