package com.peliculas.backend.repository;

import com.peliculas.backend.model.Favorito;
import com.peliculas.backend.model.TipoContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {
    Optional<Favorito> findByUserIdAndTipoContenidoAndContenidoId(UUID userId, TipoContenido tipoContenido, Long contenidoId);

    List<Favorito> findByUserIdOrderByFechaCreacionDesc(UUID userId);

    List<Favorito> findByUserIdAndTipoContenidoOrderByFechaCreacionDesc(UUID userId, TipoContenido tipoContenido);

    @Modifying
    @Query("delete from Favorito f where f.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
