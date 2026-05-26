package com.peliculas.backend.dto;

import java.util.UUID;

public class ComentarioRequest {
    private Long resenaId;
    private UUID userId;
    private String comentario;

    public Long getResenaId() {
        return resenaId;
    }

    public void setResenaId(Long resenaId) {
        this.resenaId = resenaId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
