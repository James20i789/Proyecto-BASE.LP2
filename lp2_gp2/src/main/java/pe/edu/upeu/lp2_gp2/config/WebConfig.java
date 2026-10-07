package pe.edu.upeu.lp2_gp2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${storage.location}")
    private String storageLocation;

    // 1. Configuración para las IMÁGENES
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapea la URL /images/** a la carpeta física en la raíz
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + storageLocation + "/");
    }

    // 2. Configuración para CORS (Conexión con Angular)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permitimos todo: /api/** e /images/**
                .allowedOrigins("http://localhost:4200") // El puerto de Angular
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}