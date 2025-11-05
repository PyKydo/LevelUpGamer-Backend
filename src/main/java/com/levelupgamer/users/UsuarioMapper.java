package com.levelupgamer.users;

import com.levelupgamer.users.dto.UsuarioRegistroDTO;
import com.levelupgamer.users.dto.UsuarioRespuestaDTO;

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
        UsuarioRespuestaDTO dto = new UsuarioRespuestaDTO();
        dto.setId(u.getId());
        dto.setRun(u.getRun());
        dto.setNombre(u.getNombre());
        dto.setApellidos(u.getApellidos());
        dto.setCorreo(u.getCorreo());
        dto.setRegion(u.getRegion());
        dto.setComuna(u.getComuna());
        dto.setDireccion(u.getDireccion());
        dto.setPuntosLevelUp(u.getPuntosLevelUp());
        dto.setCodigoReferido(u.getCodigoReferido());
        dto.setRol(u.getRol() != null ? u.getRol().name() : null);
        return dto;
    }
}

