package beverage_store.service;

/**
 * Service API for beverage related operations.
 */
public interface BeverageService {

    /**
     * Update the available quantity for a beverage.
     *
     * @param beverageId the id of the beverage to update
     * @param newQuantity the new in-stock quantity (must be >= 0)
     */
    void updateBeverageQuantity(Long beverageId, int newQuantity);
}
