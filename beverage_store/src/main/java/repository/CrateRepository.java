package beverage_store.repository;

import beverage_store.model.Crate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Crate entities. Ensures the related Bottle is fetched to avoid N+1 selects
 * when crates are accessed together with their bottle information.
 */
@Repository
public interface CrateRepository extends JpaRepository<Crate, Long> {

    @EntityGraph(attributePaths = {"bottle"})
    List<Crate> findAll();

    @EntityGraph(attributePaths = {"bottle"})
    Optional<Crate> findById(Long id);

    /**
     * Convenience finder to locate a crate by its contained bottle id.
     * Eagerly fetches the bottle to avoid additional queries.
     */
    @EntityGraph(attributePaths = {"bottle"})
    Optional<Crate> findByBottleId(Long bottleId);
}
