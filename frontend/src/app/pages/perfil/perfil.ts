import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';
import { SocialService, Resena } from '../../services/social';
import { CatalogoService, Pelicula, Serie } from '../../services/catalogo';

interface FavoritoConNombre {
  id: number;
  tipoContenido: 'PELICULA' | 'SERIE';
  contenidoId: number;
  titulo: string;
  imagenUrl?: string;
}

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class PerfilComponent {
  userId: string | null = null;
  nombreUsuario = '';
  resenas: Resena[] = [];
  favoritos: FavoritoConNombre[] = [];
  peliculas: Pelicula[] = [];
  series: Serie[] = [];

  constructor(
    private authService: AuthService,
    private socialService: SocialService,
    private catalogoService: CatalogoService,
  ) {}

  ngOnInit() {
    this.userId = this.authService.obtenerUserId();
    this.nombreUsuario = localStorage.getItem('nombreUsuario') ?? '';

    if (!this.userId) {
      return;
    }

    this.catalogoService.listarPeliculas().subscribe({
      next: (data) => {
        this.peliculas = data;
        this.cargarFavoritos();
      },
      error: () => {
        this.peliculas = [];
        this.cargarFavoritos();
      },
    });

    this.catalogoService.listarSeries().subscribe({
      next: (data) => (this.series = data),
      error: () => (this.series = []),
    });

    this.socialService.listarResenasUsuario(this.userId).subscribe({
      next: (data) => (this.resenas = data),
      error: () => (this.resenas = []),
    });
  }

  private cargarFavoritos() {
    if (!this.userId) return;

    this.socialService.listarFavoritos(this.userId).subscribe({
      next: (data) => {
        this.favoritos = data.map((favorito) => {
          let titulo = '';
          let imagenUrl = '';

          if (favorito.tipoContenido === 'PELICULA') {
            const pelicula = this.peliculas.find(
              (p) => p.id === favorito.contenidoId,
            );
            titulo = pelicula
              ? pelicula.titulo
              : `Película #${favorito.contenidoId}`;
            imagenUrl = pelicula ? pelicula.imagenUrl : '';
          } else {
            const serie = this.series.find(
              (s) => s.id === favorito.contenidoId,
            );
            titulo = serie ? serie.titulo : `Serie #${favorito.contenidoId}`;
            imagenUrl = serie ? serie.imagenUrl : '';
          }

          return {
            ...favorito,
            titulo,
            imagenUrl,
          };
        });
      },
      error: () => (this.favoritos = []),
    });
  }

  eliminarFavorito(favorito: FavoritoConNombre) {
    if (!this.userId) return;

    this.socialService
      .toggleFavorito({
        userId: this.userId,
        tipoContenido: favorito.tipoContenido,
        contenidoId: favorito.contenidoId,
      })
      .subscribe({
        next: () => {
          this.favoritos = this.favoritos.filter((f) => f.id !== favorito.id);
        },
        error: () => alert('No se pudo eliminar el favorito'),
      });
  }
}
