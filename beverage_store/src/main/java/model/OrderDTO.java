package beverage_store.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO used to generate order PDFs. Only includes the fields required by the PDF generator.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * Use BigDecimal for monetary values to avoid floating point issues.
     */
    @Builder.Default
    private BigDecimal totalPrice = BigDecimal.ZERO;

    /**
     * List of order line items to be rendered in the PDF.
     */
    private List<OrderListDTO> listOfItems;

    private String userEmail;
    private String postalCode;

    /**
     * ISO-8601 formatted timestamp (e.g. 2025-11-27T15:04:05Z).
     */
    private String timestamp;
}
