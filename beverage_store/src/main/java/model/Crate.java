package beverage_store.model;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Crate entity representing a package of bottles.
 * - noOfBottles is required and must be >= 1
 * - bottle relation is required
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(name = "Beverage.Crate", attributeNodes = {
        @NamedAttributeNode("bottle")
})
public class Crate extends Beverage {

    @NotNull(message = "The number of bottles is required")
    @Min(value = 1, message = "The number of bottles must be at least 1")
    @Column(name = "no_of_bottles", nullable = false)
    private Integer noOfBottles;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull(message = "Bottle is required")
    @JoinColumn(name = "bottle_id", nullable = false)
    private Bottle bottle;
}