package com.peliculas.backend.repository;

import com.peliculas.backend.model.ResenaLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResenaLikeRepository extends JpaRepository<ResenaLike, Long> {

    boolean existsByUserIdAndResena_Id(UUID userId, Long resenaId);

    Optional<ResenaLike> findByUserIdAndResena_Id(UUID userId, Long resenaId);

    Optional<ResenaLike> findByUserIdAndResena_IdAndTipoVoto(UUID userId, Long resenaId, String tipoVoto);

    @Query("select rl from ResenaLike rl join fetch rl.resena where rl.userId = :userId")
    List<ResenaLike> findAllByUserIdWithResena(@Param("userId") UUID userId);

    void deleteByResena_Id(Long resenaId);

    @Modifying
    @Query("delete from ResenaLike rl where rl.resena.id in (select r.id from Resena r where r.userId = :userId)")
    void deleteByResenaUserId(@Param("userId") UUID userId);
}
