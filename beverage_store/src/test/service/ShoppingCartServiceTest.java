package beverage_store.service;

import beverage_store.model.Bottle;
import beverage_store.model.OrderItem;
import beverage_store.service.impl.ShoppingCartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class ShoppingCartServiceTest {

    @Autowired
    private ShoppingCartServiceImpl shoppingCartService;

    private Bottle schlenkerla;
    private OrderItem sampleItem;
    private List<OrderItem> items;

    @BeforeEach
    public void initCommonUsedData() {
        // ensure a clean cart for each test
        shoppingCartService.clearAllItems();
        items = shoppingCartService.getItemsInCart();

        schlenkerla = new Bottle();
        schlenkerla.setId(1L);
        schlenkerla.setName("Schlenkerla");
        schlenkerla.setPic("https://www.getraenkewelt-weiser.de/images/product/01/85/40/18546-0-p.jpg");
        schlenkerla.setVolume(0.5);
        schlenkerla.setVolumePercent(5.1);
        schlenkerla.setPrice(0.89);
        schlenkerla.setSupplier("Rauchbierbrauerei Schlenkerla");
        schlenkerla.setInStock(438);

        sampleItem = new OrderItem(schlenkerla, 34);
    }

    @Test
    public void addItem_ShouldSuccessOnNewItem() {
        shoppingCartService.addItem(sampleItem);
        assertTrue(items.contains(sampleItem));
    }

    @Test
    public void addItem_ShouldSuccessOnExistingItem() {
        shoppingCartService.addItem(sampleItem);
        shoppingCartService.addItem(sampleItem);

        assertTrue(items.contains(sampleItem));
        assertEquals(34 * 2, items.get(0).getQuantity());
    }

    @Test
    public void updateItem_ShouldUpdateQuantity() {
        shoppingCartService.addItem(sampleItem);
        sampleItem.setQuantity(1);

        shoppingCartService.updateItem(sampleItem);

        assertTrue(items.contains(sampleItem));
        assertEquals(1, items.get(0).getQuantity());
    }

    @Test
    public void updateItem_ShouldRemoveItem() {
        shoppingCartService.addItem(sampleItem);
        sampleItem.setQuantity(0);

        shoppingCartService.updateItem(sampleItem);

        assertFalse(items.contains(sampleItem));
    }

    @Test
    public void getItemsInCart_ShouldSuccess() {
        List<OrderItem> actual = shoppingCartService.getItemsInCart();
        assertEquals(items, actual);
    }

    @Test
    public void getTotal_ShouldSuccess() {
        shoppingCartService.addItem(sampleItem);

        double actual = shoppingCartService.getTotal();
        double expected = schlenkerla.getPrice() * sampleItem.getQuantity();

        assertEquals(expected, actual, 1e-6);
    }

    @Test
    public void clearAllItems_ShouldSuccess() {
        shoppingCartService.addItem(sampleItem);

        shoppingCartService.clearAllItems();

        assertEquals(0, items.size());
    }
}
