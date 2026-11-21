package pe.edu.upeu.lp2_gp2.Controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.lp2_gp2.dto.ProductoDTO;
import pe.edu.upeu.lp2_gp2.exceptions.ResouceNotFoundException;
import pe.edu.upeu.lp2_gp2.services.IFileStorageService;
import pe.edu.upeu.lp2_gp2.services.IProductoService;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {
    private final IProductoService products;
    private final IFileStorageService files;

    public ProductoController(IProductoService products, IFileStorageService files) {
        this.products = products;
        this.files = files;
    }

    @GetMapping
    public List<ProductoDTO> listar() {
        return products.ListarTodo();
    }

    @GetMapping("/{id}")
    public ProductoDTO obtener(@PathVariable Long id) {
        return products.buscarPorId(id).orElseThrow(() -> new ResouceNotFoundException("Producto no encontrado."));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoDTO> crear(@Valid @ModelAttribute ProductoDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(save(null, dto, file));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductoDTO actualizar(@PathVariable Long id, @Valid @ModelAttribute ProductoDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return save(id, dto, file);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        if (!products.eliminar(id)) {
            throw new ResouceNotFoundException("Producto no encontrado.");
        }
    }

    private ProductoDTO save(Long id, ProductoDTO dto, MultipartFile file) {
        String image = file == null || file.isEmpty() ? null : files.store(file);
        var product = new ProductoDTO(id, dto.nombre(), dto.descripcion(), dto.precio(), dto.stock(), image);
        try {
            return id == null ? products.crear(product) : products.actualizar(id, product)
                    .orElseThrow(() -> new ResouceNotFoundException("Producto no encontrado."));
        } catch (RuntimeException ex) {
            files.delete(image);
            throw ex;
        }
    }
}
