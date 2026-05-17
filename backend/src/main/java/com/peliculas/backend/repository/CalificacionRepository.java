package com.peliculas.backend.repository;

import com.peliculas.backend.model.Calificacion;
import com.peliculas.backend.model.TipoContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    Optional<Calificacion> findByUserIdAndTipoContenidoAndContenidoId(UUID userId, TipoContenido tipoContenido, Long contenidoId);

    @Query("select avg(c.puntuacion) from Calificacion c where c.tipoContenido = :tipoContenido and c.contenidoId = :contenidoId")
    Double calcularPromedio(TipoContenido tipoContenido, Long contenidoId);

    long countByTipoContenidoAndContenidoId(TipoContenido tipoContenido, Long contenidoId);

    @Query("select c.puntuacion from Calificacion c where c.userId = :userId and c.tipoContenido = :tipoContenido and c.contenidoId = :contenidoId")
    Optional<Integer> findPuntuacionByUsuario(UUID userId, TipoContenido tipoContenido, Long contenidoId);

    @Modifying
    @Query("delete from Calificacion c where c.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
