package pe.edu.upeu.lp2_gp2.services;

import pe.edu.upeu.lp2_gp2.dto.ProductoDTO;

import java.util.List;
import java.util.Optional;

public interface IProductoService {

    // VARIABLES - INTERFACE (ARTEFACTOS - METODOS A IMPLEMENTAR) | USUARIO
    List<ProductoDTO> ListarTodo();
    Optional<ProductoDTO> buscarPorId(Long id);
    ProductoDTO crear(ProductoDTO p);
    Optional<ProductoDTO> actualizar(Long id, ProductoDTO p);
    boolean eliminar(Long id);

}