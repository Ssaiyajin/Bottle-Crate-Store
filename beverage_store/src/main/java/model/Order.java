package beverage_store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "order_table")
@Data
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(name = "Order.Order", attributeNodes = {
        @NamedAttributeNode("customer"),
        @NamedAttributeNode("items")
})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Use BigDecimal for monetary values to avoid floating point problems.
     * Initialized to ZERO so prevention of NPE when reading before calculation.
     */
    @DecimalMin(value = "0.01", message = "Price must be higher than 0")
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    private User customer;

    /**
     * Mapped by the 'order' field on OrderItem. Cascade all so items persist/merge/remove with the order.
     * Initialized to avoid null checks and to satisfy @Size validation.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OrderItem> items = new HashSet<>();

    public void addOrderItem(OrderItem item) {
        if (item == null) {
            return;
        }
        items.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        if (item == null) {
            return;
        }
        items.remove(item);
        item.setOrder(null);
    }

    /**
     * Recalculate total price from contained order items using BigDecimal arithmetic.
     */
    public void recalcTotalPrice() {
        this.totalPrice = items.stream()
                .filter(Objects::nonNull)
                .map(OrderItem::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Legacy helper retained but updated to return BigDecimal.
     */
    public BigDecimal priceTotal(Collection<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItem::getPrice)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Set<OrderItem> getItems() {
        return items;
    }

    public void setItems(Set<OrderItem> items) {
        this.items = items;
    }
}
