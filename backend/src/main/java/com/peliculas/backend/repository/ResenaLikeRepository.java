package com.peliculas.backend.repository;

import com.peliculas.backend.model.ResenaLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResenaLikeRepository extends JpaRepository<ResenaLike, Long> {

    boolean existsByUsuario_IdAndResena_Id(Long usuarioId, Long resenaId);

    ResenaLike findByUsuario_IdAndResena_Id(Long usuarioId, Long resenaId);
}