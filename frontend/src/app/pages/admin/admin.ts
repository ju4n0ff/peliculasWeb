import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth';

interface Pelicula {
  id: number;
  titulo: string;
  imagenUrl: string;
  descripcion: string;
  genero: string;
  anio: number;
}

interface Serie {
  id: number;
  titulo: string;
  imagenUrl: string;
  descripcion: string;
  genero: string;
  anio: number;
  temporadas: number;
}

interface Resena {
  id: number;
  usuarioId: number;
  nombreUsuario: string;
  titulo: string;
  comentario: string;
  tipoContenido: string;
  contenidoId: number;
  fechaCreacion: string;
}

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.html',
  styleUrl: './admin.css'
})
export class AdminComponent implements OnInit {
  tabActual: 'peliculas' | 'series' | 'resenas' = 'peliculas';
  
  peliculas: Pelicula[] = [];
  series: Serie[] = [];
  resenas: Resena[] = [];
  
  editandoPeliculaId: number | null = null;
  editandoSerieId: number | null = null;
  editandoPelicula: Pelicula = this.getEmptyPelicula();
  editandoSerie: Serie = this.getEmptySerie();
  
  nuevaPelicula: Partial<Pelicula> = {};
  nuevaSerie: Partial<Serie> = {};

  private apiPeliculas = 'http://localhost:8080/api/peliculas';
  private apiSeries = 'http://localhost:8080/api/series';
  private apiResenas = 'http://localhost:8080/api/social';

  constructor(private http: HttpClient, private authService: AuthService) {}

  ngOnInit() {
    this.cargarPeliculas();
    this.cargarSeries();
    this.cargarResenas();
  }

  getEmptyPelicula(): Pelicula {
    return { id: 0, titulo: '', imagenUrl: '', descripcion: '', genero: '', anio: 2024 };
  }

  getEmptySerie(): Serie {
    return { id: 0, titulo: '', imagenUrl: '', descripcion: '', genero: '', anio: 2024, temporadas: 1 };
  }

  cargarPeliculas() {
    this.http.get<Pelicula[]>(this.apiPeliculas).subscribe({
      next: (data) => (this.peliculas = data),
      error: () => alert('Error al cargar películas')
    });
  }

  cargarSeries() {
    this.http.get<Serie[]>(this.apiSeries).subscribe({
      next: (data) => (this.series = data),
      error: () => alert('Error al cargar series')
    });
  }

  cargarResenas() {
    this.http.get<any[]>(`${this.apiResenas}/resenas/todas`).subscribe({
      next: (data) => {
        this.resenas = data.map(r => ({
          id: r.id,
          usuarioId: r.usuarioId,
          nombreUsuario: r.nombreUsuario,
          titulo: r.titulo,
          comentario: r.comentario,
          tipoContenido: r.tipoContenido,
          contenidoId: r.contenidoId,
          fechaCreacion: r.fechaCreacion
        }));
      },
      error: (err) => console.error('Error cargando reseñas:', err)
    });
  }

  // Películas
  crearPelicula() {
    if (!this.nuevaPelicula.titulo || !this.nuevaPelicula.genero) {
      alert('Completa los campos obligatorios');
      return;
    }

    this.http.post<Pelicula>(this.apiPeliculas, {
      titulo: this.nuevaPelicula.titulo,
      descripcion: this.nuevaPelicula.descripcion || '',
      genero: this.nuevaPelicula.genero,
      anio: this.nuevaPelicula.anio || 2024,
      imagenUrl: this.nuevaPelicula.imagenUrl || ''
    }).subscribe({
      next: () => {
        this.cargarPeliculas();
        this.nuevaPelicula = {};
        alert('Película creada');
      },
      error: () => alert('Error al crear película')
    });
  }

  iniciarEdicionPelicula(pelicula: Pelicula) {
    this.editandoPeliculaId = pelicula.id;
    this.editandoPelicula = { ...pelicula };
  }

  guardarPelicula() {
    if (!this.editandoPeliculaId) return;
    
    this.http.put<Pelicula>(`${this.apiPeliculas}/${this.editandoPeliculaId}`, this.editandoPelicula).subscribe({
      next: () => {
        this.editandoPeliculaId = null;
        this.editandoPelicula = this.getEmptyPelicula();
        this.cargarPeliculas();
        alert('Película actualizada');
      },
      error: () => alert('Error al actualizar película')
    });
  }

  cancelarEdicionPelicula() {
    this.editandoPeliculaId = null;
    this.editandoPelicula = this.getEmptyPelicula();
  }

  eliminarPelicula(id: number) {
    if (!confirm('¿Eliminar esta película?')) return;
    
    this.http.delete(`${this.apiPeliculas}/${id}`).subscribe({
      next: () => {
        this.cargarPeliculas();
        alert('Película eliminada');
      },
      error: (err) => {
        console.error('Error:', err);
        alert('Error al eliminar película');
      }
    });
  }

  // Series
  crearSerie() {
    if (!this.nuevaSerie.titulo || !this.nuevaSerie.genero) {
      alert('Completa los campos obligatorios');
      return;
    }

    this.http.post<Serie>(this.apiSeries, {
      titulo: this.nuevaSerie.titulo,
      descripcion: this.nuevaSerie.descripcion || '',
      genero: this.nuevaSerie.genero,
      anio: this.nuevaSerie.anio || 2024,
      temporadas: this.nuevaSerie.temporadas || 1,
      imagenUrl: this.nuevaSerie.imagenUrl || ''
    }).subscribe({
      next: () => {
        this.cargarSeries();
        this.nuevaSerie = {};
        alert('Serie creada');
      },
      error: () => alert('Error al crear serie')
    });
  }

  iniciarEdicionSerie(serie: Serie) {
    this.editandoSerieId = serie.id;
    this.editandoSerie = { ...serie };
  }

  guardarSerie() {
    if (!this.editandoSerieId) return;
    
    this.http.put<Serie>(`${this.apiSeries}/${this.editandoSerieId}`, this.editandoSerie).subscribe({
      next: () => {
        this.editandoSerieId = null;
        this.editandoSerie = this.getEmptySerie();
        this.cargarSeries();
        alert('Serie actualizada');
      },
      error: () => alert('Error al actualizar serie')
    });
  }

  cancelarEdicionSerie() {
    this.editandoSerieId = null;
    this.editandoSerie = this.getEmptySerie();
  }

  eliminarSerie(id: number) {
    if (!confirm('¿Eliminar esta serie?')) return;
    
    this.http.delete(`${this.apiSeries}/${id}`).subscribe({
      next: () => this.cargarSeries(),
      error: () => alert('Error al eliminar serie')
    });
  }

  // Reseñas
  eliminarResena(id: number) {
    if (!confirm('¿Eliminar esta reseña?')) return;
    
    this.http.delete(`${this.apiResenas}/resenas/admin/${id}`).subscribe({
      next: () => this.cargarResenas(),
      error: () => alert('Error al eliminar reseña')
    });
  }

  cambiarTab(tab: 'peliculas' | 'series' | 'resenas') {
    this.tabActual = tab;
  }

  getContenidoTitulo(resena: Resena): string {
    if (resena.tipoContenido === 'PELICULA') {
      const p = this.peliculas.find(p => p.id === resena.contenidoId);
      return p?.titulo || 'Película #' + resena.contenidoId;
    } else {
      const s = this.series.find(s => s.id === resena.contenidoId);
      return s?.titulo || 'Serie #' + resena.contenidoId;
    }
  }
}