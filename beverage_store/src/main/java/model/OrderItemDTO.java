package beverage_store.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DTO used to update the shopping cart.
 * - quantity is required and must be >= 1
 * - beverageId is required and must be >= 1
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "beverageId is required")
    @Min(value = 1, message = "beverageId must be at least 1")
    private Long beverageId;

}
