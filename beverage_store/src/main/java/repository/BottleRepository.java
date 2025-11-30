package beverage_store.repository;

import beverage_store.model.Bottle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BottleRepository extends JpaRepository<Bottle, Long> {

    /**
     * Find a bottle by its exact name (case-insensitive).
     */
    Optional<Bottle> findByNameIgnoreCase(String name);

    /**
     * Return all bottles ordered by name (useful for UI lists).
     */
    List<Bottle> findAllByOrderByNameAsc();

    /**
     * Convenience existence check by name.
     */
    boolean existsByNameIgnoreCase(String name);
}
