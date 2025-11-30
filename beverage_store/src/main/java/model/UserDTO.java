package beverage_store.model;

import beverage_store.validator.Since;
import lombok.*;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    @NotBlank(message = "Password must be set")
    @Size(min = 6, message = "Password must be at least {min} characters")
    private String password;

    @NotNull(message = "Please enter birth date")
    @Past(message = "Birth date should be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Since("01.01.1900")
    private LocalDate birthday;

    @NotBlank(message = "Please enter e-mail")
    @Email(message = "Enter a valid e-mail")
    private String email;

    @NotBlank(message = "Please enter username")
    private String username;

    /**
     * Lists are initialized to avoid NPEs during binding / tests.
     */
    @Setter
    @Builder.Default
    private List<Address> deliveryAddresses = new ArrayList<>();

    @Setter
    @Builder.Default
    private List<Address> billingAddresses = new ArrayList<>();

    public Set<Address> getDeliveryAddressesAsSet() {
        return deliveryAddresses == null ? new HashSet<>() : new HashSet<>(deliveryAddresses);
    }

    public Set<Address> getBillingAddressesAsSet() {
        return billingAddresses == null ? new HashSet<>() : new HashSet<>(billingAddresses);
    }

    public User toUser() {
        return new User(
                this.username,
                this.password,
                "", // role is set elsewhere
                this.birthday,
                this.email,
                null,
                getBillingAddressesAsSet(),
                getDeliveryAddressesAsSet()
        );
    }
}
