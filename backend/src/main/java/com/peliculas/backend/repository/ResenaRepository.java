package com.peliculas.backend.repository;

import com.peliculas.backend.model.Resena;
import com.peliculas.backend.model.TipoContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByTipoContenidoAndContenidoIdOrderByFechaCreacionDesc(TipoContenido tipoContenido,
            Long contenidoId);

    List<Resena> findByUserIdOrderByFechaCreacionDesc(UUID userId);

    @Modifying
    @Query("delete from Resena r where r.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
