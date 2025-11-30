package beverage_store.service.impl;

import beverage_store.model.Order;
import beverage_store.model.OrderItem;
import beverage_store.service.ShoppingCartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import service.FirebaseFallbackService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.math.BigDecimal;

/**
 * Session-scoped shopping cart implementation.
 * Uses a map keyed by beverage id to reliably merge/update items.
 */
@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {

    // preserve insertion order for predictable display
    private final Map<Long, OrderItem> itemsByBeverageId = new LinkedHashMap<>();

    @Autowired
    private FirebaseFallbackService firebaseFallbackService;

    private final ObjectMapper mapper = new ObjectMapper();

    private void persistOrderBackup(Order order) {
        try {
            if (firebaseFallbackService.isAvailable()) {
                // existing logic: write to Firestore if you want
                // firebaseFallbackService.getFirestore().ifPresent(f -> { ... });
            } else {
                Path out = firebaseFallbackService.getLocalRoot().resolve("orders");
                Files.createDirectories(out);
                String filename = "order_" + Instant.now().toEpochMilli() + ".json";
                byte[] json = mapper.writeValueAsBytes(order);
                Files.write(out.resolve(filename), json);
            }
        } catch (Exception e) {
            // ignore fallback failure - maybe log
        }
    }

    @Override
    public synchronized void addItem(OrderItem item) {
        if (item == null || item.getBeverage() == null || item.getBeverage().getId() == null) {
            return;
        }
        int qty = Math.max(0, item.getQuantity());
        if (qty == 0) {
            return;
        }

        Long bevId = item.getBeverage().getId();
        OrderItem existing = itemsByBeverageId.get(bevId);
        if (existing != null) {
            existing.increaseQuantityBy(qty);
        } else {
            // store a copy to avoid accidental external mutations
            itemsByBeverageId.put(bevId, new OrderItem(item.getBeverage(), qty));
        }
    }

    @Override
    public synchronized void updateItem(OrderItem item) {
        if (item == null || item.getBeverage() == null || item.getBeverage().getId() == null) {
            return;
        }
        Long bevId = item.getBeverage().getId();
        if (!itemsByBeverageId.containsKey(bevId)) {
            return;
        }
        int qty = Math.max(0, item.getQuantity());
        if (qty == 0) {
            itemsByBeverageId.remove(bevId);
        } else {
            OrderItem existing = itemsByBeverageId.get(bevId);
            existing.setQuantity(qty);
        }
    }

    @Override
    public synchronized List<OrderItem> getItemsInCart() {
        return Collections.unmodifiableList(new ArrayList<>(itemsByBeverageId.values()));
    }

    @Override
    public double getTotal() {
        return itemsByBeverageId.values().stream()
                .map(OrderItem::getPrice) // or the appropriate method returning BigDecimal
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }

    @Override
    public synchronized void clearAllItems() {
        itemsByBeverageId.clear();
    }

    @Override
    public synchronized void removeItem(OrderItem item) {
        if (item == null || item.getBeverage() == null || item.getBeverage().getId() == null) {
            return;
        }
        itemsByBeverageId.remove(item.getBeverage().getId());
    }
}
