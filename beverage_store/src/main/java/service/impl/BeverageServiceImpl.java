package beverage_store.service.impl;

import beverage_store.model.Beverage;
import beverage_store.repository.BeverageRepository;
import beverage_store.service.BeverageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class BeverageServiceImpl implements BeverageService {

    private final BeverageRepository beverageRepository;

    @Autowired
    public BeverageServiceImpl(BeverageRepository beverageRepository) {
        this.beverageRepository = beverageRepository;
    }

    @Override
    public void updateBeverageQuantity(Long beverageId, int newQuantity) {
        if (beverageId == null) {
            log.warn("updateBeverageQuantity called with null beverageId");
            return;
        }

        Optional<Beverage> opt = beverageRepository.findById(beverageId);
        if (!opt.isPresent()) {
            log.warn("Beverage with id {} not found; no update performed", beverageId);
            return;
        }

        Beverage existing = opt.get();
        int q = Math.max(0, newQuantity);
        existing.setInStock(q);
        beverageRepository.save(existing);
        log.info("Updated beverage id {} stock to {}", beverageId, q);
    }
}
