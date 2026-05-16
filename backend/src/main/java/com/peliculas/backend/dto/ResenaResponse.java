package com.peliculas.backend.dto;

import com.peliculas.backend.model.Resena;

import java.time.LocalDateTime;

public class ResenaResponse {
    private Long id;
    private Long usuarioId;
    private String nombreUsuario;
    private String titulo;
    private String comentario;
    private int likes;
    private LocalDateTime fechaCreacion;
    private boolean liked;

    public static ResenaResponse fromEntity(Resena resena) {
        ResenaResponse response = new ResenaResponse();
        response.id = resena.getId();
        response.usuarioId = resena.getUsuario().getId();
        response.nombreUsuario = resena.getUsuario().getNombreUsuario();
        response.titulo = resena.getTitulo();
        response.comentario = resena.getComentario();
        response.likes = resena.getLikes();
        response.fechaCreacion = resena.getFechaCreacion();

        return response;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public Integer getLikes() {
        return likes;
    }
}
