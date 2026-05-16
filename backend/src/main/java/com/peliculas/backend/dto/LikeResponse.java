package com.peliculas.backend.dto;

public class LikeResponse {
    private int likes;
    private boolean liked;

    public LikeResponse(int likes, boolean liked) {
        this.likes = likes;
        this.liked = liked;
    }

    public int getLikes() {
        return likes;
    }

    public boolean isLiked() {
        return liked;
    }
}