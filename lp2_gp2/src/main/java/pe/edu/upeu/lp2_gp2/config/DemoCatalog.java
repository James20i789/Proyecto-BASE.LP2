package pe.edu.upeu.lp2_gp2.config;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pe.edu.upeu.lp2_gp2.entity.Producto;
import pe.edu.upeu.lp2_gp2.repository.ProductoRepository;

@Configuration
@Profile("demo")
class DemoCatalog {
    @Bean
    CommandLineRunner seedCatalog(ProductoRepository products) {
        return args -> {
            if (products.count() == 0) {
                products.saveAll(List.of(
                        new Producto(0, "Pachamanca de la casa", "Carnes y papas andinas con hierbas aromáticas.",
                                new BigDecimal("32.00"), 18, "205baa54-74d3-4f1c-898a-23f34bd87a86_Pachamanca.jpg"),
                        new Producto(0, "Trío marino", "Tres clásicos de la costa peruana en un solo plato.",
                                new BigDecimal("29.00"), 15, "92710586-6f05-4b98-a5da-484f13aa8592_Trio.Marino.jfif"),
                        new Producto(0, "Combo cevichero", "Ceviche fresco, chicharrón y guarniciones de la casa.",
                                new BigDecimal("27.00"), 20, "e0630f50-a835-40e7-8b74-06156b92d26b_Combo.jpg")));
            }
        };
    }
}
