package cloudfunction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    private String id;
    private String postalCode;
    private Instant timeStamp;
    private List<OrderItem> listOfItems;

    public Order() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public Instant getTimeStamp() { return timeStamp; }
    public void setTimeStamp(Instant timeStamp) { this.timeStamp = timeStamp; }

    public List<OrderItem> getListOfItems() { return listOfItems; }
    public void setListOfItems(List<OrderItem> listOfItems) { this.listOfItems = listOfItems; }
}
