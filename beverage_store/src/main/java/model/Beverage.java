package beverage_store.model;

import lombok.Data;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Beverage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Price must be at least 0.01")
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @NotBlank(message = "Please enter Name")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]*$", message = "Only letters, numbers and spaces are allowed")
    private String name;

    @Pattern(regexp = "(https:\\/\\/).*\\.(?:jpg|gif|png)$", message = "Must be a valid URL to a picture.")
    private String pic;

    @Min(value = 0, message = "The stock supply can not be negative")
    private int inStock = 0;
}