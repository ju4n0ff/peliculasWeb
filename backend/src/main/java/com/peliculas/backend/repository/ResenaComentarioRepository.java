package com.peliculas.backend.repository;

import com.peliculas.backend.model.ResenaComentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ResenaComentarioRepository extends JpaRepository<ResenaComentario, Long> {

    List<ResenaComentario> findByResenaIdOrderByFechaCreacionAsc(Long resenaId);

    @Modifying
    @Query("delete from ResenaComentario rc where rc.resena.id in (select r.id from Resena r where r.userId = :userId)")
    void deleteByResenaUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("delete from ResenaComentario rc where rc.resena.id = :resenaId")
    void deleteByResenaId(@Param("resenaId") Long resenaId);
}
