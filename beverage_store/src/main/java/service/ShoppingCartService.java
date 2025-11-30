package beverage_store.service;

import beverage_store.model.OrderItem;

import java.util.List;

/**
 * Service that manages the current shopping cart (session-scoped).
 */
public interface ShoppingCartService {

    /**
     * Add an item to the cart. Implementations should merge with existing item
     * for the same beverage (increase quantity) when appropriate.
     *
     * @param item item to add
     */
    void addItem(OrderItem item);

    /**
     * Update an existing item in the cart (e.g. change quantity).
     *
     * @param item updated item
     */
    void updateItem(OrderItem item);

    /**
     * Return all items currently in the cart.
     *
     * @return list of order items
     */
    List<OrderItem> getItemsInCart();

    /**
     * Calculate and return the total price for items in the cart.
     *
     * @return total price
     */
    double getTotal();

    /**
     * Remove all items from the cart.
     */
    void clearAllItems();

    /**
     * Remove an item from the cart. Implementations should remove the item
     * matching the beverage id (quantity doesn't need to match).
     *
     * @param item item to remove
     */
    void removeItem(OrderItem item);
}
