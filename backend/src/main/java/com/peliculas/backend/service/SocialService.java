package com.peliculas.backend.service;

import com.peliculas.backend.dto.CalificacionRequest;
import com.peliculas.backend.dto.CalificacionResumenResponse;
import com.peliculas.backend.dto.FavoritoRequest;
import com.peliculas.backend.dto.ResenaRequest;
import com.peliculas.backend.dto.ResenaResponse;
import com.peliculas.backend.model.*;
import com.peliculas.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.peliculas.backend.dto.LikeResponse;
import java.util.List;

@Service
public class SocialService {

    private final UsuarioRepository usuarioRepository;
    private final PeliculaRepository peliculaRepository;
    private final SerieRepository serieRepository;
    private final ResenaRepository resenaRepository;
    private final ResenaLikeRepository resenaLikeRepository;
    private final CalificacionRepository calificacionRepository;
    private final FavoritoRepository favoritoRepository;

    public SocialService(
            UsuarioRepository usuarioRepository,
            PeliculaRepository peliculaRepository,
            SerieRepository serieRepository,
            ResenaRepository resenaRepository,
            ResenaLikeRepository resenaLikeRepository,
            CalificacionRepository calificacionRepository,
            FavoritoRepository favoritoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.resenaLikeRepository = resenaLikeRepository;
        this.peliculaRepository = peliculaRepository;
        this.serieRepository = serieRepository;
        this.resenaRepository = resenaRepository;
        this.calificacionRepository = calificacionRepository;
        this.favoritoRepository = favoritoRepository;
    }

    @Transactional
    public ResenaResponse crearResena(ResenaRequest request) {
        Usuario usuario = buscarUsuario(request.getUsuarioId());
        validarContenido(request.getTipoContenido(), request.getContenidoId());

        Resena resena = new Resena();
        resena.setUsuario(usuario);
        resena.setTipoContenido(request.getTipoContenido());
        resena.setContenidoId(request.getContenidoId());
        resena.setTitulo(request.getTitulo());
        resena.setComentario(request.getComentario());

        Resena guardada = resenaRepository.save(resena);

        return ResenaResponse.fromEntity(guardada);
    }

    public List<ResenaResponse> listarResenasConLike(
            TipoContenido tipoContenido,
            Long contenidoId,
            Long usuarioId) {

        List<Resena> resenas = resenaRepository
                .findByTipoContenidoAndContenidoIdOrderByFechaCreacionDesc(tipoContenido, contenidoId);

        return resenas.stream().map(resena -> {

            ResenaResponse dto = ResenaResponse.fromEntity(resena);
            boolean liked = false;

            if (usuarioId != null) {
                liked = resenaLikeRepository.existsByUsuario_IdAndResena_Id(usuarioId, resena.getId());
            }

            dto.setLiked(liked);

            return dto;
        }).toList();
    }

    public List<Resena> listarResenasUsuario(Long usuarioId) {
        return resenaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    @Transactional
    public void eliminarResena(Long resenaId, Long usuarioId) {
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new RuntimeException("Resena no encontrada"));

        if (!resena.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No puedes eliminar una resena de otro usuario");
        }

        resenaRepository.delete(resena);
    }

    @Transactional
    public void eliminarResenaAdmin(Long resenaId) {
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new RuntimeException("Resena no encontrada"));
        resenaRepository.delete(resena);
    }

    public List<Resena> listarTodasResenas() {
        return resenaRepository.findAll();
    }

    @Transactional
    public CalificacionResumenResponse calificar(CalificacionRequest request) {
        if (request.getPuntuacion() == null || request.getPuntuacion() < 1 || request.getPuntuacion() > 5) {
            throw new RuntimeException("La puntuacion debe estar entre 1 y 5");
        }

        Usuario usuario = buscarUsuario(request.getUsuarioId());
        validarContenido(request.getTipoContenido(), request.getContenidoId());

        Calificacion calificacion = calificacionRepository
                .findByUsuarioIdAndTipoContenidoAndContenidoId(usuario.getId(), request.getTipoContenido(),
                        request.getContenidoId())
                .orElseGet(Calificacion::new);

        calificacion.setUsuario(usuario);
        calificacion.setTipoContenido(request.getTipoContenido());
        calificacion.setContenidoId(request.getContenidoId());
        calificacion.setPuntuacion(request.getPuntuacion());
        calificacionRepository.save(calificacion);

        return obtenerResumenCalificacion(request.getTipoContenido(), request.getContenidoId(), request.getUsuarioId());
    }

    public CalificacionResumenResponse obtenerResumenCalificacion(TipoContenido tipoContenido, Long contenidoId,
            Long usuarioId) {
        Double promedio = calificacionRepository.calcularPromedio(tipoContenido, contenidoId);
        long total = calificacionRepository.countByTipoContenidoAndContenidoId(tipoContenido, contenidoId);
        Integer miPuntuacion = null;

        if (usuarioId != null) {
            miPuntuacion = calificacionRepository
                    .findPuntuacionByUsuario(usuarioId, tipoContenido, contenidoId)
                    .orElse(null);
        }

        return new CalificacionResumenResponse(promedio == null ? 0.0 : promedio, total, miPuntuacion);
    }

    @Transactional
    public boolean alternarFavorito(FavoritoRequest request) {
        Usuario usuario = buscarUsuario(request.getUsuarioId());
        validarContenido(request.getTipoContenido(), request.getContenidoId());

        Favorito existente = favoritoRepository
                .findByUsuarioIdAndTipoContenidoAndContenidoId(usuario.getId(), request.getTipoContenido(),
                        request.getContenidoId())
                .orElse(null);

        if (existente != null) {
            favoritoRepository.delete(existente);
            return false;
        }

        Favorito favorito = new Favorito();
        favorito.setUsuario(usuario);
        favorito.setTipoContenido(request.getTipoContenido());
        favorito.setContenidoId(request.getContenidoId());
        favoritoRepository.save(favorito);
        return true;
    }

    public List<Favorito> listarFavoritos(Long usuarioId, TipoContenido tipoContenido) {
        if (tipoContenido == null) {
            return favoritoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        }
        return favoritoRepository.findByUsuarioIdAndTipoContenidoOrderByFechaCreacionDesc(usuarioId, tipoContenido);
    }

    public boolean esFavorito(Long usuarioId, TipoContenido tipoContenido, Long contenidoId) {
        return favoritoRepository
                .findByUsuarioIdAndTipoContenidoAndContenidoId(usuarioId, tipoContenido, contenidoId)
                .isPresent();
    }

    @Transactional
    public LikeResponse darLike(Long resenaId, Long usuarioId) {

        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));

        boolean yaExiste = resenaLikeRepository
                .existsByUsuario_IdAndResena_Id(usuarioId, resenaId);

        if (yaExiste) {

            ResenaLike like = resenaLikeRepository
                    .findByUsuario_IdAndResena_Id(usuarioId, resenaId);

            if (like != null) {
                resenaLikeRepository.delete(like);
            }

            resena.setLikes(Math.max(0, resena.getLikes() - 1));

        } else {

            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            ResenaLike like = new ResenaLike();
            like.setUsuario(usuario);
            like.setResena(resena);

            resenaLikeRepository.save(like);

            resena.setLikes(resena.getLikes() + 1);
        }

        resenaRepository.save(resena);

        return new LikeResponse(resena.getLikes(), !yaExiste);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private void validarContenido(TipoContenido tipoContenido, Long contenidoId) {
        boolean existe = switch (tipoContenido) {
            case PELICULA -> peliculaRepository.existsById(contenidoId);
            case SERIE -> serieRepository.existsById(contenidoId);
        };

        if (!existe) {
            throw new RuntimeException("El contenido no existe");
        }
    }
}
