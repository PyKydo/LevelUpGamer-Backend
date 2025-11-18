package com.levelupgamer.usuarios;

import com.levelupgamer.usuarios.dto.UsuarioRegistroDTO;
import com.levelupgamer.usuarios.dto.UsuarioRespuestaDTO;

public class UsuarioMapper {
    public static Usuario toEntity(UsuarioRegistroDTO dto) {
        Usuario u = new Usuario();
        u.setRun(dto.getRun());
        u.setNombre(dto.getNombre());
        u.setApellidos(dto.getApellidos());
        u.setCorreo(dto.getCorreo());
        u.setContrasena(dto.getContrasena());
        u.setFechaNacimiento(dto.getFechaNacimiento());
        u.setRegion(dto.getRegion());
        u.setComuna(dto.getComuna());
        u.setDireccion(dto.getDireccion());
        u.setCodigoReferido(dto.getCodigoReferido());
        u.setActivo(true);
        return u;
    }

    public static UsuarioRespuestaDTO toDTO(Usuario u) {
        return toDTO(u, null);
    }

    public static UsuarioRespuestaDTO toDTO(Usuario u, Integer puntos) {
        UsuarioRespuestaDTO dto = new UsuarioRespuestaDTO();
        dto.setId(u.getId());
        dto.setRun(u.getRun());
        dto.setNombre(u.getNombre());
        dto.setApellidos(u.getApellidos());
        dto.setCorreo(u.getCorreo());
        dto.setRegion(u.getRegion());
        dto.setComuna(u.getComuna());
        dto.setDireccion(u.getDireccion());
        dto.setCodigoReferido(u.getCodigoReferido());
        dto.setRol(u.getRoles() != null ? u.getRoles().toString() : null);
        dto.setFechaNacimiento(u.getFechaNacimiento());
        dto.setPuntosLevelUp(puntos);
        return dto;
    }
}
