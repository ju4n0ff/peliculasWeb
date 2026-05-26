package com.peliculas.backend.dto;

import com.peliculas.backend.model.ResenaComentario;
import java.time.LocalDateTime;
import java.util.UUID;

public class ComentarioResponse {
    private Long id;
    private Long resenaId;
    private UUID userId;
    private String nombreUsuario;
    private String comentario;
    private LocalDateTime fechaCreacion;

    public static ComentarioResponse fromEntity(ResenaComentario comentario) {
        ComentarioResponse response = new ComentarioResponse();
        response.id = comentario.getId();
        response.resenaId = comentario.getResena().getId();
        response.userId = comentario.getUserId();
        response.nombreUsuario = comentario.getNombreUsuario();
        response.comentario = comentario.getComentario();
        response.fechaCreacion = comentario.getFechaCreacion();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getResenaId() {
        return resenaId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
