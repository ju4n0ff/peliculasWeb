package com.peliculas.backend.service;

import com.peliculas.backend.dto.CalificacionRequest;
import com.peliculas.backend.dto.CalificacionResumenResponse;
import com.peliculas.backend.dto.ComentarioRequest;
import com.peliculas.backend.dto.ComentarioResponse;
import com.peliculas.backend.dto.FavoritoRequest;
import com.peliculas.backend.dto.ResenaRequest;
import com.peliculas.backend.dto.ResenaResponse;
import com.peliculas.backend.model.*;
import com.peliculas.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.peliculas.backend.dto.LikeResponse;
import java.util.List;
import java.util.UUID;

@Service
public class SocialService {

    private final UsuarioRepository usuarioRepository;
    private final PeliculaRepository peliculaRepository;
    private final SerieRepository serieRepository;
    private final ResenaRepository resenaRepository;
    private final ResenaLikeRepository resenaLikeRepository;
    private final CalificacionRepository calificacionRepository;
    private final FavoritoRepository favoritoRepository;
    private final ResenaComentarioRepository resenaComentarioRepository;

    public SocialService(
            UsuarioRepository usuarioRepository,
            PeliculaRepository peliculaRepository,
            SerieRepository serieRepository,
            ResenaRepository resenaRepository,
            ResenaLikeRepository resenaLikeRepository,
            CalificacionRepository calificacionRepository,
            FavoritoRepository favoritoRepository,
            ResenaComentarioRepository resenaComentarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.resenaLikeRepository = resenaLikeRepository;
        this.peliculaRepository = peliculaRepository;
        this.serieRepository = serieRepository;
        this.resenaRepository = resenaRepository;
        this.calificacionRepository = calificacionRepository;
        this.favoritoRepository = favoritoRepository;
        this.resenaComentarioRepository = resenaComentarioRepository;
    }

    @Transactional
    public ResenaResponse crearResena(ResenaRequest request) {
        Usuario usuario = buscarUsuario(request.getUserId());
        validarContenido(request.getTipoContenido(), request.getContenidoId());

        Resena resena = new Resena();
        resena.setUserId(usuario.getUserId());
        resena.setNombreUsuario(usuario.getNombreUsuario());
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
            UUID userId) {

        List<Resena> resenas = resenaRepository
                .findByTipoContenidoAndContenidoIdOrderByFechaCreacionDesc(tipoContenido, contenidoId);

        return resenas.stream().map(resena -> {
            ResenaResponse dto = ResenaResponse.fromEntity(resena);
            String votoActual = null;

            if (userId != null) {
                votoActual = resenaLikeRepository
                        .findByUserIdAndResena_Id(userId, resena.getId())
                        .map(ResenaLike::getTipoVoto)
                        .orElse(null);
            }

            dto.setVotoActual(votoActual);

            return dto;
        }).toList();
    }

    public List<Resena> listarResenasUsuario(UUID userId) {
        return resenaRepository.findByUserIdOrderByFechaCreacionDesc(userId);
    }

    @Transactional
    public void eliminarResena(Long resenaId, UUID userId) {
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new RuntimeException("Resena no encontrada"));

        if (!resena.getUserId().equals(userId)) {
            throw new RuntimeException("No puedes eliminar una resena de otro usuario");
        }

        resenaComentarioRepository.deleteByResenaId(resenaId);
        resenaLikeRepository.deleteByResena_Id(resenaId);
        resenaRepository.delete(resena);
    }

    @Transactional
    public void eliminarResenaAdmin(Long resenaId) {
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new RuntimeException("Resena no encontrada"));
        resenaComentarioRepository.deleteByResenaId(resenaId);
        resenaLikeRepository.deleteByResena_Id(resenaId);
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

        UUID userId = request.getUserId();
        validarContenido(request.getTipoContenido(), request.getContenidoId());

        Calificacion calificacion = calificacionRepository
                .findByUserIdAndTipoContenidoAndContenidoId(userId, request.getTipoContenido(),
                        request.getContenidoId())
                .orElseGet(Calificacion::new);

        calificacion.setUserId(userId);
        calificacion.setTipoContenido(request.getTipoContenido());
        calificacion.setContenidoId(request.getContenidoId());
        calificacion.setPuntuacion(request.getPuntuacion());
        calificacionRepository.save(calificacion);

        return obtenerResumenCalificacion(request.getTipoContenido(), request.getContenidoId(), request.getUserId());
    }

    public CalificacionResumenResponse obtenerResumenCalificacion(TipoContenido tipoContenido, Long contenidoId,
            UUID userId) {
        Double promedio = calificacionRepository.calcularPromedio(tipoContenido, contenidoId);
        long total = calificacionRepository.countByTipoContenidoAndContenidoId(tipoContenido, contenidoId);
        Integer miPuntuacion = null;

        if (userId != null) {
            miPuntuacion = calificacionRepository
                    .findPuntuacionByUsuario(userId, tipoContenido, contenidoId)
                    .orElse(null);
        }

        return new CalificacionResumenResponse(promedio == null ? 0.0 : promedio, total, miPuntuacion);
    }

    @Transactional
    public boolean alternarFavorito(FavoritoRequest request) {
        UUID userId = request.getUserId();
        validarContenido(request.getTipoContenido(), request.getContenidoId());

        Favorito existente = favoritoRepository
                .findByUserIdAndTipoContenidoAndContenidoId(userId, request.getTipoContenido(),
                        request.getContenidoId())
                .orElse(null);

        if (existente != null) {
            favoritoRepository.delete(existente);
            return false;
        }

        Favorito favorito = new Favorito();
        favorito.setUserId(userId);
        favorito.setTipoContenido(request.getTipoContenido());
        favorito.setContenidoId(request.getContenidoId());
        favoritoRepository.save(favorito);
        return true;
    }

    public List<Favorito> listarFavoritos(UUID userId, TipoContenido tipoContenido) {
        if (tipoContenido == null) {
            return favoritoRepository.findByUserIdOrderByFechaCreacionDesc(userId);
        }
        return favoritoRepository.findByUserIdAndTipoContenidoOrderByFechaCreacionDesc(userId, tipoContenido);
    }

    public boolean esFavorito(UUID userId, TipoContenido tipoContenido, Long contenidoId) {
        return favoritoRepository
                .findByUserIdAndTipoContenidoAndContenidoId(userId, tipoContenido, contenidoId)
                .isPresent();
    }

    @Transactional
    public LikeResponse votarResena(Long resenaId, UUID userId, String tipoVoto) {
        String voto = tipoVoto == null ? "" : tipoVoto.trim().toUpperCase();

        if (!"LIKE".equals(voto) && !"DISLIKE".equals(voto)) {
            throw new RuntimeException("tipoVoto debe ser LIKE o DISLIKE");
        }

        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));

        var votoExistente = resenaLikeRepository.findByUserIdAndResena_Id(userId, resenaId);

        String votoAnterior = votoExistente.map(ResenaLike::getTipoVoto).orElse(null);

        if (votoAnterior != null) {
            if (votoAnterior.equals(voto)) {
                resenaLikeRepository.delete(votoExistente.get());

                if ("LIKE".equals(voto)) {
                    resena.setLikes(Math.max(0, resena.getLikes() - 1));
                } else {
                    resena.setDislikes(Math.max(0, resena.getDislikes() - 1));
                }
            } else {
                ResenaLike like = votoExistente.get();
                like.setTipoVoto(voto);
                resenaLikeRepository.save(like);

                if ("LIKE".equals(voto)) {
                    resena.setLikes(resena.getLikes() + 1);
                    resena.setDislikes(Math.max(0, resena.getDislikes() - 1));
                } else {
                    resena.setDislikes(resena.getDislikes() + 1);
                    resena.setLikes(Math.max(0, resena.getLikes() - 1));
                }
            }
        } else {
            ResenaLike nuevoVoto = new ResenaLike();
            nuevoVoto.setResena(resena);
            nuevoVoto.setUserId(userId);
            nuevoVoto.setTipoVoto(voto);
            resenaLikeRepository.save(nuevoVoto);

            if ("LIKE".equals(voto)) {
                resena.setLikes(resena.getLikes() + 1);
            } else {
                resena.setDislikes(resena.getDislikes() + 1);
            }
        }

        resenaRepository.save(resena);

        String nuevoVotoActual = resenaLikeRepository.findByUserIdAndResena_Id(userId, resenaId)
                .map(ResenaLike::getTipoVoto)
                .orElse(null);

        return new LikeResponse(resena.getLikes(), resena.getDislikes(), nuevoVotoActual);
    }

    @Transactional
    public ComentarioResponse crearComentario(ComentarioRequest request) {
        Resena resena = resenaRepository.findById(request.getResenaId())
                .orElseThrow(() -> new RuntimeException("Resena no encontrada"));

        Usuario usuario = buscarUsuario(request.getUserId());

        ResenaComentario comentario = new ResenaComentario();
        comentario.setResena(resena);
        comentario.setUserId(usuario.getUserId());
        comentario.setNombreUsuario(usuario.getNombreUsuario());
        comentario.setComentario(request.getComentario());

        return ComentarioResponse.fromEntity(resenaComentarioRepository.save(comentario));
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarComentarios(Long resenaId) {
        return resenaComentarioRepository.findByResenaIdOrderByFechaCreacionAsc(resenaId)
                .stream()
                .map(ComentarioResponse::fromEntity)
                .toList();
    }

    private Usuario buscarUsuario(UUID userId) {
        return usuarioRepository.findByUserId(userId)
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
