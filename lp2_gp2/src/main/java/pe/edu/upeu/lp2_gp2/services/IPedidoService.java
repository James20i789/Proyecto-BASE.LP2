package pe.edu.upeu.lp2_gp2.services;


import pe.edu.upeu.lp2_gp2.dto.PedidoResponseDTO;
import pe.edu.upeu.lp2_gp2.dto.VentaDTO;

// VARIABLES - INTERFACE (ARTEFACTOS - METODOS A IMPLEMENTAR) | USUARIO
public interface IPedidoService {
    PedidoResponseDTO realizarPedido(VentaDTO dto);


}
