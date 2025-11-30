package beverage_store;

import beverage_store.model.*;
import beverage_store.repository.BottleRepository;
import beverage_store.repository.CrateRepository;
import beverage_store.repository.OrderRepository;
import beverage_store.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.math.BigDecimal;

@SpringBootApplication
public class BeverageStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeverageStoreApplication.class, args);
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner demo(OrderRepository orderRepository, UserService userService, BottleRepository bottleRepository, CrateRepository crateRepository) {
        return (args) -> {
            //Schlenkerla
            Bottle schlenkerla = new Bottle();
            schlenkerla.setName("Schlenkerla");
            schlenkerla.setPic("https://www.getraenkewelt-weiser.de/images/product/01/85/40/18546-0-p.jpg");
            schlenkerla.setVolume(0.5);
            schlenkerla.setVolumePercent(5.1);
            schlenkerla.setPrice(BigDecimal.valueOf(0.89));
            schlenkerla.setSupplier("Rauchbierbrauerei Schlenkerla");
            schlenkerla.setInStock(438);
            //schlenkerla = bottleRepository.save(schlenkerla);


            //Crate Schlenkerla
            Crate crateSchlenkerla = new Crate();
            crateSchlenkerla.setName("20 Crate Schlenkerla");
            crateSchlenkerla.setPic("https://www.getraenkedienst.com/media/image/34/b1/39/Brauerei_Heller_Schlenkerla_Aecht_Schlenkerla_Rauchbier_Maerzen_20_x_0_5l.jpg");
            crateSchlenkerla.setNoOfBottles(20);
            crateSchlenkerla.setPrice(BigDecimal.valueOf(18.39));
            crateSchlenkerla.setInStock(13);
            crateSchlenkerla.setBottle(schlenkerla);
            //crateSchlenkerla = crateRepository.save(crateSchlenkerla);


            //Gartenlimonade
            Bottle limo = new Bottle();
            limo.setName("Garten Limonade");
            limo.setPic("https://cdn02.plentymarkets.com/q7p0kwea05gv/item/images/9484/full/37170-1.jpg");
            limo.setVolume(0.5);
            limo.setVolumePercent(0.0);
            limo.setPrice(BigDecimal.valueOf(0.79));
            limo.setSupplier("Bad Brambacher");
            limo.setInStock(234);
            //limo = bottleRepository.save(limo);




            //Crate Gartenlimonade
            Crate crateLimo = new Crate();
            crateLimo.setName("20 Crate Gartenlimonade");
            crateLimo.setPic("https://www.beowein.de/WebRoot/Store8/Shops/Shop538/5AF9/CEBE/0C0C/98A7/69CF/AC14/500C/943C/2470_bb_gartenlimo_zitrone_05_glas_fl_ka.png");
            crateLimo.setNoOfBottles(20);
            crateLimo.setPrice(BigDecimal.valueOf(15.60));
            crateLimo.setInStock(8);
            crateLimo.setBottle(limo);
            //crateLimo = crateRepository.save(crateLimo);




            //Orderitems
            //OrderItem s = new OrderItem(schlenkerla, 12);
            //OrderItem cS = new OrderItem(crateSchlenkerla, 24);
            //OrderItem l = new OrderItem(limo, 3);
            //OrderItem cL = new OrderItem(crateLimo, 4);


            Address address = new Address(null, "An der Spinnerei", "13", "96049");
            Address address2 = new Address(null, "An der Test", "15", "96000");
            User user = new User();
            user.setUsername("Max");
            user.setPassword("123456");
            user.setRole("CUSTOMER");
            user.setBirthday(LocalDate.of(1996, 8, 2));

            user.setBillingaddresses(Collections.singleton(address));
            user.setDeliveryaddresses(Collections.singleton(address2));
            user = userService.registerUser(user);


            User admin = new User();
            admin.setUsername("Admin");
            admin.setPassword("123456");
            admin.setRole("ADMIN");
            admin.setBirthday(LocalDate.of(1996, 8, 2));
            admin.setBillingaddresses(Collections.singleton(address));
            admin.setDeliveryaddresses(Collections.singleton(address2));
            admin = userService.registerUser(admin);

            //Order
            //Order order = new Order();
            //order.setPrice(45);
            //order.setCustomer(user);
            //order.addOrderItem(s);
            //order.addOrderItem(cS);
            //order.addOrderItem(l);
            //order.addOrderItem(cL);

            //orderRepository.save(order);

        };
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
