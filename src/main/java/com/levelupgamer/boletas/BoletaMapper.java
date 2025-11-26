package com.levelupgamer.boletas;

import com.levelupgamer.boletas.dto.BoletaDetalleRespuestaDTO;
import com.levelupgamer.boletas.dto.BoletaRespuestaDTO;
import com.levelupgamer.boletas.dto.BoletaUsuarioDTO;
import com.levelupgamer.usuarios.Usuario;
import java.util.List;
import java.util.stream.Collectors;

public class BoletaMapper {
    public static BoletaRespuestaDTO toDTO(Boleta boleta) {
        BoletaRespuestaDTO dto = new BoletaRespuestaDTO();
        dto.setId(boleta.getId());
        dto.setUsuarioId(boleta.getUsuario() != null ? boleta.getUsuario().getId() : null);
        dto.setUsuario(toUsuarioDTO(boleta.getUsuario()));
        dto.setDetalles(toDetalleDTOList(boleta.getDetalles()));
        dto.setTotal(boleta.getTotal());
        dto.setTotalAntesDescuentos(boleta.getTotalAntesDescuentos());
        dto.setDescuentoCupon(boleta.getDescuentoCuponAplicado());
        dto.setDescuentoDuoc(boleta.getDescuentoDuocAplicado());
        dto.setCuponCodigo(boleta.getCupon() != null ? boleta.getCupon().getCodigo() : null);
        dto.setCuponId(boleta.getCupon() != null ? boleta.getCupon().getId() : null);
        dto.setFecha(boleta.getFecha());
        dto.setEstado(boleta.getEstado() != null ? boleta.getEstado().name() : null);
        return dto;
    }

    public static List<BoletaDetalleRespuestaDTO> toDetalleDTOList(List<BoletaDetalle> detalles) {
        if (detalles == null) return null;
        return detalles.stream().map(BoletaMapper::toDetalleDTO).collect(Collectors.toList());
    }

    public static BoletaDetalleRespuestaDTO toDetalleDTO(BoletaDetalle detalle) {
        BoletaDetalleRespuestaDTO dto = new BoletaDetalleRespuestaDTO();
        dto.setProductoId(detalle.getProducto() != null ? detalle.getProducto().getId() : null);
        dto.setNombreProducto(detalle.getProducto() != null ? detalle.getProducto().getNombre() : null);
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }

    private static BoletaUsuarioDTO toUsuarioDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return BoletaUsuarioDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellidos(usuario.getApellidos())
                .correo(usuario.getCorreo())
                .build();
    }
}

