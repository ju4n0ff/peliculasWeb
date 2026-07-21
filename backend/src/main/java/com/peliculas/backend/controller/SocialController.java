package com.peliculas.backend.controller;

import com.peliculas.backend.dto.*;
import com.peliculas.backend.model.Favorito;
import com.peliculas.backend.model.TipoContenido;
import com.peliculas.backend.service.SocialService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/social")
@CrossOrigin(origins = {"http://localhost:4200", "https://peliculas-web-tau.vercel.app", "https://peliculasweb-backend.onrender.com"})
public class SocialController {

    private final SocialService socialService;

    public SocialController(SocialService socialService) {
        this.socialService = socialService;
    }

    @GetMapping("/resenas")
    public List<ResenaResponse> listarResenas(
            @RequestParam TipoContenido tipoContenido,
            @RequestParam Long contenidoId,
            @RequestParam(required = false) UUID userId) {

        return socialService.listarResenasConLike(tipoContenido, contenidoId, userId);
    }

    @PostMapping("/resenas")
    public ResenaResponse crearResena(@RequestBody ResenaRequest request) {
        return socialService.crearResena(request);
    }

    @GetMapping("/resenas/usuario/{userId}")
    public List<ResenaResponse> listarResenasUsuario(@PathVariable UUID userId) {
        return socialService.listarResenasUsuario(userId)
                .stream()
                .map(ResenaResponse::fromEntity)
                .toList();
    }

    @PostMapping("/resenas/{resenaId}/voto")
    public LikeResponse votar(
            @PathVariable Long resenaId,
            @RequestParam UUID userId,
            @RequestParam String tipoVoto) {

        return socialService.votarResena(resenaId, userId, tipoVoto);
    }

    @DeleteMapping("/resenas/{resenaId}")
    public void eliminarResena(@PathVariable Long resenaId, @RequestParam UUID userId) {
        socialService.eliminarResena(resenaId, userId);
    }

    @DeleteMapping("/resenas/admin/{resenaId}")
    public void eliminarResenaAdmin(@PathVariable Long resenaId) {
        socialService.eliminarResenaAdmin(resenaId);
    }

    @GetMapping("/resenas/todas")
    public List<ResenaResponse> listarTodasResenas() {
        return socialService.listarTodasResenas()
                .stream()
                .map(ResenaResponse::fromEntity)
                .toList();
    }

    @PostMapping("/calificaciones")
    public CalificacionResumenResponse calificar(@RequestBody CalificacionRequest request) {
        return socialService.calificar(request);
    }

    @GetMapping("/calificaciones/resumen")
    public CalificacionResumenResponse obtenerResumen(
            @RequestParam TipoContenido tipoContenido,
            @RequestParam Long contenidoId,
            @RequestParam(required = false) UUID userId) {
        return socialService.obtenerResumenCalificacion(tipoContenido, contenidoId, userId);
    }

    @PostMapping("/favoritos/toggle")
    public Map<String, Boolean> alternarFavorito(@RequestBody FavoritoRequest request) {
        boolean favorito = socialService.alternarFavorito(request);
        return Map.of("favorito", favorito);
    }

    @GetMapping("/favoritos")
    public List<Favorito> listarFavoritos(
            @RequestParam UUID userId,
            @RequestParam(required = false) TipoContenido tipoContenido) {
        return socialService.listarFavoritos(userId, tipoContenido);
    }

    @GetMapping("/favoritos/existe")
    public Map<String, Boolean> esFavorito(
            @RequestParam UUID userId,
            @RequestParam TipoContenido tipoContenido,
            @RequestParam Long contenidoId) {
        return Map.of("favorito", socialService.esFavorito(userId, tipoContenido, contenidoId));
    }

    @PostMapping("/resenas/comentarios")
    public ComentarioResponse crearComentario(@RequestBody ComentarioRequest request) {
        return socialService.crearComentario(request);
    }

    @GetMapping("/resenas/{resenaId}/comentarios")
    public List<ComentarioResponse> listarComentarios(@PathVariable Long resenaId) {
        return socialService.listarComentarios(resenaId);
    }
}