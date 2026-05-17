package com.peliculas.backend.dto;

import com.peliculas.backend.model.TipoContenido;
import java.util.UUID;

public class FavoritoRequest {
    private UUID userId;
    private TipoContenido tipoContenido;
    private Long contenidoId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public TipoContenido getTipoContenido() {
        return tipoContenido;
    }

    public void setTipoContenido(TipoContenido tipoContenido) {
        this.tipoContenido = tipoContenido;
    }

    public Long getContenidoId() {
        return contenidoId;
    }

    public void setContenidoId(Long contenidoId) {
        this.contenidoId = contenidoId;
    }
}