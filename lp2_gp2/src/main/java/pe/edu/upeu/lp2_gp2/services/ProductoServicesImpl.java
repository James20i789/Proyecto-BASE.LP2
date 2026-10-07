package pe.edu.upeu.lp2_gp2.services;


import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lp2_gp2.dto.ProductoDTO;
import pe.edu.upeu.lp2_gp2.entity.Producto;
import pe.edu.upeu.lp2_gp2.repository.ProductoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoServicesImpl implements IProductoService {
    private final ProductoRepository productoRepository;
    private final IFileStorageService fileStorageService;


    public ProductoServicesImpl(ProductoRepository productoRepository, IFileStorageService fileStorageService) {
        this.productoRepository = productoRepository;
        this.fileStorageService = fileStorageService;
    }
    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> ListarTodo() {
        return productoRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoDTO> buscarPorId(Long id) {
        return productoRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public ProductoDTO crear(ProductoDTO p) {
        Producto pro = new Producto();
        pro.setNombre(p.nombre());
        pro.setDescripcion(p.descripcion());
        pro.setPrecio(p.precio());
        pro.setStock(p.stock());
        pro.setImagen(p.imagen());
        return convertToDTO(productoRepository.save(pro));
    }

    @Override
    @Transactional
    public Optional<ProductoDTO> actualizar(Long id, ProductoDTO dto) {
        return productoRepository.findById(id).map(p -> {
            // Lógica de limpieza de archivos:
            // Si el nombre de imagen que viene en el DTO es distinto al de la BD,
            // borramos el archivo físico anterior para no dejar basura.
            if (p.getImagen() != null && !p.getImagen().equals(dto.imagen())) {
                fileStorageService.delete(p.getImagen());
            }

            p.setNombre(dto.nombre());
            p.setDescripcion(dto.descripcion());
            p.setPrecio(dto.precio());
            p.setStock(dto.stock());
            p.setImagen(dto.imagen()); // Seteamos el nuevo nombre que viene del controlador

            return convertToDTO(productoRepository.save(p));
        });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {
        return productoRepository.findById(id).map(p -> {
            // Borrar archivo físico
            if (p.getImagen() != null) {
                fileStorageService.delete(p.getImagen());
            }
            productoRepository.delete(p);
            return true;
        }).orElse(false);
    }

    private ProductoDTO convertToDTO(Producto p) {
        // En lugar de URL completa, devolvemos solo la ruta relativa
        // Esto devolverá: "/ecommerce_lp2/images/nombre_foto.jpg"
        String relativePath = "/lp2_gp2/images/" + p.getImagen();

        return new ProductoDTO(
                p.getIdProducto(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getStock(),
                relativePath // Enviamos la ruta relativa
        );
    }
}