package com.peliculas.backend.controller;

import com.peliculas.backend.model.Serie;
import com.peliculas.backend.service.SerieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/series")
@CrossOrigin(origins = {"http://localhost:4200", "https://peliculas-web-tau.vercel.app"})
public class SerieController {

    @Autowired
    private SerieService serieService;

    @GetMapping
    public List<Serie> getAllSeries() {
        return serieService.getAllSeries();
    }

    @GetMapping("/{id}")
    public Serie getSerieById(@PathVariable Long id) {
        return serieService.getSerieById(id);
    }

    @PostMapping
    public Serie createSerie(@RequestBody Serie serie) {
        return serieService.saveSerie(serie);
    }

    @PutMapping("/{id}")
    public Serie updateSerie(@PathVariable Long id, @RequestBody Serie serie) {
        Serie existente = serieService.getSerieById(id);
        if (existente == null) {
            throw new RuntimeException("Serie no encontrada");
        }
        existente.setTitulo(serie.getTitulo());
        existente.setDescripcion(serie.getDescripcion());
        existente.setGenero(serie.getGenero());
        existente.setAnio(serie.getAnio());
        existente.setTemporadas(serie.getTemporadas());
        existente.setImagenUrl(serie.getImagenUrl());
        return serieService.saveSerie(existente);
    }

    @DeleteMapping("/{id}")
    public void deleteSerie(@PathVariable Long id) {
        serieService.deleteSerie(id);
    }
}