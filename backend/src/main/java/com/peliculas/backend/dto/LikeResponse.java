package com.peliculas.backend.dto;

public class LikeResponse {
    private int likes;
    private int dislikes;
    private String votoActual;

    public LikeResponse(int likes, int dislikes, String votoActual) {
        this.likes = likes;
        this.dislikes = dislikes;
        this.votoActual = votoActual;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public String getVotoActual() {
        return votoActual;
    }
}
