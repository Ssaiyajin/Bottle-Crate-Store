package beverage_store.model;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Bottle extends Beverage {

    @NotNull(message = "Volume is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Volume must be higher than 0")
    @Column(nullable = false)
    private Double volume;

    @Builder.Default
    @Column(name = "is_alcoholic", nullable = false)
    private Boolean alcoholic = Boolean.FALSE;

    @DecimalMin(value = "0.0", inclusive = true, message = "Volume percent must be >= 0")
    @DecimalMax(value = "100.0", message = "Volume percent must be <= 100")
    @Column(name = "volume_percent")
    private Double volumePercent;

    @NotBlank(message = "Please enter Supplier")
    @Column(nullable = false)
    private String supplier;

    /**
     * Setters with defensive validation to keep the entity in a valid state.
     */
    public void setVolume(Double volume) {
        if (volume == null || volume <= 0.0) {
            throw new IllegalArgumentException("Volume must be higher than 0");
        }
        this.volume = volume;
    }

    /**
     * Sets the volumePercent and updates alcoholic flag.
     * Accepts null to indicate 'unknown' (will set alcoholic = false).
     */
    public void setVolumePercent(Double percent) {
        if (percent == null) {
            this.volumePercent = null;
            this.alcoholic = Boolean.FALSE;
            return;
        }
        if (percent < 0.0 || percent > 100.0) {
            throw new IllegalArgumentException("Volume percent must be between 0 and 100");
        }
        this.volumePercent = percent;
        this.alcoholic = percent > 0.0;
    }

    /**
     * Convenience boolean accessor to avoid Lombok naming issues.
     */
    public boolean isAlcoholic() {
        return Boolean.TRUE.equals(this.alcoholic);
    }
}