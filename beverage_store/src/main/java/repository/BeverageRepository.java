package beverage_store.repository;

import beverage_store.model.Beverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Beverage root entity.
 * Provides a couple of convenience queries used by controllers/services.
 */
@Repository
public interface BeverageRepository extends JpaRepository<Beverage, Long> {

    /**
     * Find a beverage by name (case-insensitive).
     */
    Optional<Beverage> findByNameIgnoreCase(String name);

    /**
     * Return all beverages ordered by name (useful for UI lists).
     */
    List<Beverage> findAllByOrderByNameAsc();

    /**
     * Convenience existence check by name.
     */
    boolean existsByNameIgnoreCase(String name);
}
