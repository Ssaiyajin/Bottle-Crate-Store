package beverage_store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(name = "Order.OrderItem", attributeNodes = {
        @NamedAttributeNode("beverage"),
        @NamedAttributeNode("order")
})
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Pattern(regexp = "^[0-9]*$", message = "Only digits are allowed.")
    @Column(length = 20)
    private String position;

    @DecimalMin(value = "0.01", message = "Order Item Price must be higher than 0")
    @Column(precision = 19, scale = 4)
    private BigDecimal price = BigDecimal.ZERO;

    @Min(value = 1, message = "Order Item Quantity must be at least 1")
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Beverage beverage;

    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    public OrderItem(Beverage beverage, int quantity) {
        this.beverage = beverage;
        this.quantity = quantity;
        // beverage.getPrice() returns BigDecimal; multiply with quantity
        this.price = this.beverage.getPrice().multiply(BigDecimal.valueOf(this.quantity));
    }

    public void increaseQuantityBy(int quantity) {
        setQuantity(this.quantity + quantity);
    }

    /**
     * Sets the quantity to @quantity and calculates the new price.
     * Validates quantity and handles null beverage safely.
     *
     * @param quantity must be >= 1
     */
    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.quantity = quantity;
        recalcPriceFromBeverage();
    }

    public void setPrice(BigDecimal price) {
        if (price == null) {
            this.price = BigDecimal.ZERO;
            return;
        }
        this.price = price;
    }

    private void recalcPriceFromBeverage() {
        if (this.beverage == null) {
            this.price = BigDecimal.ZERO;
            return;
        }
        // beverage.getPrice() expected to be numeric (double/BigDecimal). Use BigDecimal safe conversion.
        this.price = this.beverage.getPrice().multiply(BigDecimal.valueOf(this.quantity));
    }

    /**
     * Two OrderItems are considered equal when they reference the same Beverage id.
     * Safely handles nulls.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof OrderItem)) return false;
        OrderItem other = (OrderItem) object;
        Long thisBeverageId = (this.beverage == null) ? null : this.beverage.getId();
        Long otherBeverageId = (other.beverage == null) ? null : other.beverage.getId();
        return Objects.equals(thisBeverageId, otherBeverageId);
    }

    @Override
    public int hashCode() {
        Long bevId = (this.beverage == null) ? null : this.beverage.getId();
        return bevId != null ? bevId.hashCode() : 0;
    }

    @Override
    public String toString() {
        String bevName = (beverage == null) ? "null" : beverage.getName();
        return String.format("OrderItem: Id: %s, Position: %s, Quantity: %d, Price: %s, Beverage: %s",
                this.id, this.position, this.quantity, this.price, bevName);
    }
}
