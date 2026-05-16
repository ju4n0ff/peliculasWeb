package com.peliculas.backend.repository;

import com.peliculas.backend.model.Resena;
import com.peliculas.backend.model.TipoContenido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByTipoContenidoAndContenidoIdOrderByFechaCreacionDesc(TipoContenido tipoContenido,
            Long contenidoId);

    List<Resena> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
}
