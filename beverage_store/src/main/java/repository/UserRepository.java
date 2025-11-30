package beverage_store.repository;

import beverage_store.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entities.  Provides a method that fetches related
 * associations to avoid N+1 selects when the caller needs addresses/orders.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Load a user together with common related entities (billing/delivery addresses and orders).
     * Adjust attributePaths if your entity uses different field names.
     */
    @EntityGraph(attributePaths = {
            "billingaddresses",
            "deliveryaddresses",
            "orders",
            "orders.items",
            "orders.items.beverage"
    })
    Optional<User> getUserWithEntitiesByUsername(String username);

    boolean existsByUsername(String username);
}
