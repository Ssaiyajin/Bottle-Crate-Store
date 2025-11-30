package cloudfunction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItem {
    private String beverageName;
    private String beverageId;
    private int quantity;

    public OrderItem() {}

    public String getBeverageName() { return beverageName; }
    public void setBeverageName(String beverageName) { this.beverageName = beverageName; }

    public String getBeverageId() { return beverageId; }
    public void setBeverageId(String beverageId) { this.beverageId = beverageId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
