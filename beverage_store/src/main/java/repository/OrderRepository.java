package beverage_store.repository;

import beverage_store.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Load orders together with their items and referenced beverages to avoid N+1 selects.
     */
    @Override
    @EntityGraph(attributePaths = {"items", "items.beverage", "customer"})
    List<Order> findAll();

    /**
     * Load a single order together with its items and referenced beverages.
     */
    @Override
    @EntityGraph(attributePaths = {"items", "items.beverage", "customer"})
    Optional<Order> findById(Long id);
}
