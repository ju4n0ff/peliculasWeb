package com.peliculas.backend.controller;

import com.peliculas.backend.model.Pelicula;
import com.peliculas.backend.service.PeliculaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/peliculas")
@CrossOrigin(origins = {"http://localhost:4200", "https://peliculas-web-tau.vercel.app"})
public class PeliculaController {

    @Autowired
    private PeliculaService peliculaService;

    @GetMapping
    public List<Pelicula> getAllPeliculas() {
        return peliculaService.getAllPeliculas();
    }

    @GetMapping("/{id}")
    public Pelicula getPeliculaById(@PathVariable Long id) {
        return peliculaService.getPeliculaById(id);
    }

    @PostMapping
    public Pelicula createPelicula(@RequestBody Pelicula pelicula) {
        return peliculaService.savePelicula(pelicula);
    }

    @PutMapping("/{id}")
    public Pelicula updatePelicula(@PathVariable Long id, @RequestBody Pelicula pelicula) {
        Pelicula existente = peliculaService.getPeliculaById(id);
        if (existente == null) {
            throw new RuntimeException("Película no encontrada");
        }
        existente.setTitulo(pelicula.getTitulo());
        existente.setDescripcion(pelicula.getDescripcion());
        existente.setGenero(pelicula.getGenero());
        existente.setAnio(pelicula.getAnio());
        existente.setImagenUrl(pelicula.getImagenUrl());
        return peliculaService.savePelicula(existente);
    }

    @DeleteMapping("/{id}")
    public void deletePelicula(@PathVariable Long id) {
        peliculaService.deletePelicula(id);
    }
}