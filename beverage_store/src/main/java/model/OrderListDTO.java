package beverage_store.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO used for PDF generation containing minimal order item information.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private double price;
    private int quantity;
    private String beverageName;
    private String beveragePic;
    private Long beverageId;
}
