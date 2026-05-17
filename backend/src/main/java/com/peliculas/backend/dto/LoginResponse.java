package com.peliculas.backend.dto;

import java.util.UUID;

public class LoginResponse {
    private String token;
    private UUID userId;
    private String nombreUsuario;
    private String rol;

    public LoginResponse(String token, UUID userId, String nombreUsuario, String rol) {
        this.token = token;
        this.userId = userId;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getRol() {
        return rol;
    }
}