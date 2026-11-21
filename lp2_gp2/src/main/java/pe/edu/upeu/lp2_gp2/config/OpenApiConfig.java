package pe.edu.upeu.lp2_gp2.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("Cocina UPeU")
                .version("1.0").description("API del catálogo, las sesiones y los pedidos."));
    }
}
