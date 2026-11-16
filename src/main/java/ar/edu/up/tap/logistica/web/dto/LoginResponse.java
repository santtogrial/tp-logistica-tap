package ar.edu.up.tap.logistica.web.dto;

import ar.edu.up.tap.logistica.dominio.Rol;

public record LoginResponse(String token, String tipo, long expiraEnMinutos, String nombre, Rol rol) {
}
