package beverage_store.model;

import beverage_store.validator.Since;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.*;

/**
 * Application user entity implementing Spring Security's UserDetails.
 * Collections are initialized to avoid NPEs during binding/tests.
 */
@Entity
@Table(name = "user_table")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NamedEntityGraph(name = "User.withAssociations", attributeNodes = {
        @NamedAttributeNode("billingaddresses"),
        @NamedAttributeNode("deliveryaddresses"),
        @NamedAttributeNode("orders")}
)
public class User implements UserDetails {

    @Id
    @EqualsAndHashCode.Include
    @NotBlank(message = "Please enter Username")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Please enter password")
    private String password;

    private String role;

    @Past(message = "Birth date should be less than current date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Since("01.01.1900")
    private LocalDate birthday;

    @Email(message = "Enter valid e-mail")
    private String email;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // use Lombok builder default so the initializer isn't ignored
    @lombok.Builder.Default
    private Set<Order> orders = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_billing_address",
            joinColumns = @JoinColumn(name = "user_username"),
            inverseJoinColumns = @JoinColumn(name = "address_id"))
    @Builder.Default
    private Set<Address> billingaddresses = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_delivery_address",
            joinColumns = @JoinColumn(name = "user_username"),
            inverseJoinColumns = @JoinColumn(name = "address_id"))
    @Builder.Default
    private Set<Address> deliveryaddresses = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String r = (this.role == null || this.role.isBlank()) ? "CUSTOMER" : this.role;
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + r));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public String toString() {
        return String.format("User{username=%s, role=%s, birthday=%s}", username, role, birthday);
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public void addOrder(Order order) {
        orders.add(order);
        order.setCustomer(this);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
        order.setCustomer(null);
    }
}
