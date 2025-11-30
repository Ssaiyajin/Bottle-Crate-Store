package beverage_store.service;

import beverage_store.model.Bottle;
import beverage_store.model.Beverage;
import beverage_store.repository.BeverageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
public class BeverageServiceTest {

    @MockBean
    private BeverageRepository beverageRepository;

    @Autowired
    private BeverageService beverageService;

    private Bottle defaultBottle;

    @BeforeEach
    public void initCommonUsedData() {
        defaultBottle = createBottle(1L, 438);
    }

    private Bottle createBottle(long id, int inStock) {
        Bottle b = new Bottle();
        b.setId(id);
        b.setName("Schlenkerla");
        b.setPic("https://www.getraenkewelt-weiser.de/images/product/01/85/40/18546-0-p.jpg");
        b.setVolume(0.5);
        b.setVolumePercent(5.1);
        b.setPrice(0.89);
        b.setSupplier("Rauchbierbrauerei Schlenkerla");
        b.setInStock(inStock);
        return b;
    }

    @Test
    public void updateBeverageQuantity_ShouldSaveUpdatedValue() {
        when(this.beverageRepository.findById(defaultBottle.getId())).thenReturn(Optional.of(defaultBottle));

        beverageService.updateBeverageQuantity(defaultBottle.getId(), 100);

        // service may check existing value and then re-read before saving depending on implementation
        verify(this.beverageRepository, times(2)).findById(defaultBottle.getId());
        ArgumentCaptor<Beverage> captor = ArgumentCaptor.forClass(Beverage.class);
        verify(this.beverageRepository, times(1)).save(captor.capture());
        Beverage saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(100, saved.getInStock());
        assertEquals(100, defaultBottle.getInStock());
    }

    @Test
    public void updateBeverageQuantity_DecreaseShouldSave() {
        when(this.beverageRepository.findById(defaultBottle.getId())).thenReturn(Optional.of(defaultBottle));

        beverageService.updateBeverageQuantity(defaultBottle.getId(), 200);

        // be flexible about how many times findById is called
        verify(this.beverageRepository, atLeastOnce()).findById(defaultBottle.getId());
        ArgumentCaptor<Beverage> captor = ArgumentCaptor.forClass(Beverage.class);
        verify(this.beverageRepository, times(1)).save(captor.capture());
        Beverage saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(200, saved.getInStock());
        assertEquals(200, defaultBottle.getInStock());
    }

    @Test
    public void updateBeverageQuantity_MultipleSequentialUpdates_SaveEachTime() {
        when(this.beverageRepository.findById(defaultBottle.getId())).thenReturn(Optional.of(defaultBottle));

        beverageService.updateBeverageQuantity(defaultBottle.getId(), 300);
        beverageService.updateBeverageQuantity(defaultBottle.getId(), 50);

        verify(this.beverageRepository, atLeastOnce()).findById(defaultBottle.getId());
        ArgumentCaptor<Beverage> captor = ArgumentCaptor.forClass(Beverage.class);
        verify(this.beverageRepository, times(2)).save(captor.capture());

        List<Integer> savedValues = captor.getAllValues().stream()
                .map(Beverage::getInStock)
                .collect(Collectors.toList());

        assertEquals(List.of(300, 50), savedValues);
        assertEquals(50, defaultBottle.getInStock());
    }

    @Test
    public void updateBeverageQuantity_NoSaveIfQuantityUnchanged() {
        Bottle bottleWith100 = createBottle(1L, 100);
        when(this.beverageRepository.findById(bottleWith100.getId())).thenReturn(Optional.of(bottleWith100));

        // attempt to set to the same quantity
        beverageService.updateBeverageQuantity(bottleWith100.getId(), 100);

        // service should at least read current value but not save when unchanged
        verify(this.beverageRepository, atLeastOnce()).findById(bottleWith100.getId());
        verify(this.beverageRepository, never()).save(any());
    }

    @Test
    public void updateBeverageQuantity_ShouldFailWhenBeverageDoesntExists() {
        when(this.beverageRepository.findById(2L)).thenReturn(Optional.empty());

        beverageService.updateBeverageQuantity(2L, 100);

        verify(this.beverageRepository, times(1)).findById(2L);
        verify(this.beverageRepository, never()).save(any());
    }
}
