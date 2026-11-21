package pe.edu.upeu.lp2_gp2.services;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.lp2_gp2.dto.ProductoDTO;
import pe.edu.upeu.lp2_gp2.entity.Producto;
import pe.edu.upeu.lp2_gp2.repository.DetallePedidoRepository;
import pe.edu.upeu.lp2_gp2.repository.ProductoRepository;

@Service
public class ProductoServicesImpl implements IProductoService {
    private final ProductoRepository products;
    private final DetallePedidoRepository details;
    private final IFileStorageService files;

    public ProductoServicesImpl(ProductoRepository products, DetallePedidoRepository details, IFileStorageService files) {
        this.products = products;
        this.details = details;
        this.files = files;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> ListarTodo() {
        return products.findAll().stream().map(this::convert).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoDTO> buscarPorId(Long id) {
        return products.findById(id).map(this::convert);
    }

    @Override
    @Transactional
    public ProductoDTO crear(ProductoDTO dto) {
        Producto product = new Producto();
        apply(product, dto);
        return convert(products.save(product));
    }

    @Override
    @Transactional
    public Optional<ProductoDTO> actualizar(Long id, ProductoDTO dto) {
        return products.lockById(id).map(product -> {
            if (dto.imagen() != null) {
                deleteAfterCommit(filename(product.getImagen()));
            }
            apply(product, dto);
            return convert(product);
        });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {
        return products.lockById(id).map(product -> {
            if (details.existsByProductoIdProducto(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Este producto está asociado a pedidos. Puedes ajustar su stock a cero.");
            }
            products.delete(product);
            deleteAfterCommit(filename(product.getImagen()));
            return true;
        }).orElse(false);
    }

    private void apply(Producto product, ProductoDTO dto) {
        product.setNombre(dto.nombre().strip());
        product.setDescripcion(dto.descripcion());
        product.setPrecio(dto.precio());
        product.setStock(dto.stock());
        if (dto.imagen() != null) {
            product.setImagen(dto.imagen());
        }
    }

    private ProductoDTO convert(Producto product) {
        String filename = filename(product.getImagen());
        String relativePath = filename == null ? null : "/lp2_gp2/images/" + filename;
        return new ProductoDTO(product.getIdProducto(), product.getNombre(), product.getDescripcion(),
                product.getPrecio(), product.getStock(), relativePath);
    }

    private String filename(String image) {
        if (image == null || image.isBlank() || "no-image.png".equals(image)) {
            return null;
        }
        return image.substring(image.lastIndexOf('/') + 1);
    }

    private void deleteAfterCommit(String image) {
        if (image == null) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                files.delete(image);
            }
        });
    }
}
