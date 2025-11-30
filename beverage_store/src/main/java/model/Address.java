package beverage_store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name = "address")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Street must be set")
    @Column(nullable = false)
    private String street;

    @NotBlank(message = "House number must be set")
    @Column(name = "house_number", nullable = false)
    private String houseNumber;

    @NotBlank(message = "Postal code must be set")
    @Pattern(regexp = "\\d{5}", message = "Postal code must be exactly 5 digits")
    @Size(min = 5, max = 5)
    @Column(name = "postal_code", nullable = false, length = 5)
    private String postalCode;
}
