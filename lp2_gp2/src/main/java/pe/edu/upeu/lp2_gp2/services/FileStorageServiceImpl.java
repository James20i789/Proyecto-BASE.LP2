package pe.edu.upeu.lp2_gp2.services;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileStorageServiceImpl implements IFileStorageService {
    private static final Logger LOG = LoggerFactory.getLogger(FileStorageServiceImpl.class);
    private static final Map<String, String> FORMATS = Map.of("JPEG", ".jpg", "png", ".png");
    private final Path storage;

    public FileStorageServiceImpl(@Value("${storage.location}") String location) {
        this.storage = Path.of(location).toAbsolutePath().normalize();
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(storage);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo inicializar la carpeta de imágenes", ex);
        }
    }

    @Override
    public String store(MultipartFile file) {
        String filename = UUID.randomUUID() + imageExtension(file);
        try (var input = file.getInputStream()) {
            Files.copy(input, storage.resolve(filename));
            return filename;
        } catch (IOException ex) {
            delete(filename);
            throw new IllegalStateException("No se pudo guardar la imagen", ex);
        }
    }

    private String imageExtension(MultipartFile file) {
        try (var input = file.getInputStream(); var image = new MemoryCacheImageInputStream(input)) {
            var readers = ImageIO.getImageReaders(image);
            if (readers.hasNext()) {
                var reader = readers.next();
                try {
                    reader.setInput(image);
                    String extension = FORMATS.get(reader.getFormatName());
                    if (extension != null && reader.getWidth(0) <= 8000 && reader.getHeight(0) <= 8000) {
                        return extension;
                    }
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException ex) {
            throw invalidImage();
        }
        throw invalidImage();
    }

    private ResponseStatusException invalidImage() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Selecciona una imagen JPEG o PNG válida de hasta 8000 × 8000 píxeles.");
    }

    @Override
    public void delete(String filename) {
        if (filename == null || filename.isBlank() || filename.contains("/") || filename.contains("\\")
                || filename.equals(".") || filename.equals("..")) {
            return;
        }
        try {
            Files.deleteIfExists(storage.resolve(filename));
        } catch (IOException ex) {
            LOG.warn("No se pudo eliminar la imagen {}", filename, ex);
        }
    }
}
