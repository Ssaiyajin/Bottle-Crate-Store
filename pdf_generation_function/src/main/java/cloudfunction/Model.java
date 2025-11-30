package cloudfunction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Model {
    private String name;
    private int quantity;
    private BigDecimal price;
    private String pic;

    public Model() {
        this.price = BigDecimal.ZERO;
    }

    public Model(String name, int quantity, BigDecimal price, String pic) {
        this.name = name;
        setQuantity(quantity);
        this.price = price == null ? BigDecimal.ZERO : price;
        this.pic = pic;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("quantity cannot be negative");
        this.quantity = quantity;
    }

    public BigDecimal getPrice() { return price == null ? BigDecimal.ZERO : price; }
    public void setPrice(BigDecimal price) { this.price = price == null ? BigDecimal.ZERO : price; }
    public void setPrice(String price) { this.price = (price == null || price.isBlank()) ? BigDecimal.ZERO : new BigDecimal(price); }

    public String getPic() { return pic; }
    public void setPic(String pic) { this.pic = pic; }

    public BigDecimal getTotalPrice() { return getPrice().multiply(BigDecimal.valueOf(quantity)); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Model model = (Model) o;
        return quantity == model.quantity &&
               Objects.equals(name, model.name) &&
               Objects.equals(getPrice(), model.getPrice()) &&
               Objects.equals(pic, model.pic);
    }

    @Override
    public int hashCode() { return Objects.hash(name, quantity, getPrice(), pic); }

    @Override
    public String toString() {
        return "Model{" +
               "name='" + name + '\'' +
               ", quantity=" + quantity +
               ", price=" + getPrice() +
               ", pic='" + pic + '\'' +
               '}';
    }
}
