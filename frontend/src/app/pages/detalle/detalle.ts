import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CatalogoService, Pelicula, Serie } from '../../services/catalogo';
import { AuthService } from '../../services/auth';
import { SocialService, CalificacionResumen, Resena, Comentario, TipoContenido } from '../../services/social';

@Component({
  selector: 'app-detalle',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './detalle.html',
  styleUrl: './detalle.css'
})
export class DetalleComponent implements OnInit {
  tipo: 'pelicula' | 'serie' = 'pelicula';
  contenidoId: number = 0;
  
  pelicula: Pelicula | null = null;
  serie: Serie | null = null;
  
  resenas: Resena[] = [];
  resumen: CalificacionResumen | null = null;
  miPuntuacion = 5;
  tituloResena = '';
  comentarioResena = '';
  esFavorito = false;
  cargando = true;
  comentarios: { [resenaId: number]: Comentario[] } = {};
  comentariosVisibles: { [resenaId: number]: boolean } = {};
  textoComentario: { [resenaId: number]: string } = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private catalogoService: CatalogoService,
    private socialService: SocialService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.tipo = params['tipo'] as 'pelicula' | 'serie';
      const slug = params['slug'];
      const match = slug.match(/^(\d+)(?:-(.+))?$/);
      this.contenidoId = match ? parseInt(match[1], 10) : 0;
      this.cargarContenido();
    });
  }

  private cargarContenido() {
    this.cargando = true;
    
    if (this.tipo === 'pelicula') {
      this.catalogoService.listarPeliculas().subscribe({
        next: (data) => {
          this.pelicula = data.find(p => p.id === this.contenidoId) || null;
          this.cargarSocial();
        },
        error: () => {
          this.pelicula = null;
          this.cargando = false;
        }
      });
    } else {
      this.catalogoService.listarSeries().subscribe({
        next: (data) => {
          this.serie = data.find(s => s.id === this.contenidoId) || null;
          this.cargarSocial();
        },
        error: () => {
          this.serie = null;
          this.cargando = false;
        }
      });
    }
  }

private cargarSocial() {
    const tipoContenido: TipoContenido = this.tipo === 'pelicula' ? 'PELICULA' : 'SERIE';
    const userId = this.authService.obtenerUserId() ?? undefined;

    this.socialService.listarResenas(tipoContenido, this.contenidoId, userId).subscribe({
      next: (data) => {
        this.resenas = data;
      },
      error: () => {
        this.resenas = [];
      }
    });

    this.socialService.obtenerResumen(tipoContenido, this.contenidoId, userId).subscribe({
      next: (data) => {
        this.resumen = data;
        this.miPuntuacion = data.miPuntuacion ?? 5;
      },
      error: () => (this.resumen = null)
    });

    if (userId) {
      this.socialService.esFavorito(userId, tipoContenido, this.contenidoId).subscribe({
        next: (data) => (this.esFavorito = data.favorito),
        error: () => (this.esFavorito = false)
      });
    }

    this.cargando = false;
  }

  enviarCalificacion() {
    const userId = this.authService.obtenerUserId();
    if (!userId) {
      alert('Inicia sesión para calificar');
      return;
    }

    const tipoContenido: TipoContenido = this.tipo === 'pelicula' ? 'PELICULA' : 'SERIE';

    this.socialService.calificar({
      userId,
      tipoContenido,
      contenidoId: this.contenidoId,
      puntuacion: this.miPuntuacion
    }).subscribe({
      next: (data) => (this.resumen = data),
      error: () => alert('No se pudo guardar la calificación')
    });
  }

  enviarResena() {
    const userId = this.authService.obtenerUserId();
    if (!userId) {
      alert('Inicia sesión para publicar una reseña');
      return;
    }

    if (!this.tituloResena.trim() || !this.comentarioResena.trim()) {
      alert('Completa el título y comentario');
      return;
    }

    const tipoContenido: TipoContenido = this.tipo === 'pelicula' ? 'PELICULA' : 'SERIE';

    this.socialService.crearResena({
      userId,
      tipoContenido,
      contenidoId: this.contenidoId,
      titulo: this.tituloResena.trim(),
      comentario: this.comentarioResena.trim()
    }).subscribe({
      next: () => {
        this.tituloResena = '';
        this.comentarioResena = '';
        this.cargarSocial();
      },
      error: () => alert('No se pudo publicar la reseña')
    });
  }

toggleFavorito() {
    const userId = this.authService.obtenerUserId();
    if (!userId) {
      alert('Inicia sesión para agregar favoritos');
      return;
    }

    const tipoContenido: TipoContenido = this.tipo === 'pelicula' ? 'PELICULA' : 'SERIE';

    this.socialService.toggleFavorito({
      userId,
      tipoContenido,
      contenidoId: this.contenidoId
    }).subscribe({
      next: (res) => (this.esFavorito = res.favorito),
      error: () => alert('No se pudo actualizar favoritos')
    });
  }

  votar(resena: Resena, tipoVoto: 'LIKE' | 'DISLIKE') {
    const userId = this.authService.obtenerUserId();

    if (!userId) {
      alert('Inicia sesión para votar');
      return;
    }

    this.socialService.votarResena(resena.id, userId, tipoVoto).subscribe({
      next: (res) => {
        resena.likes = res.likes;
        resena.dislikes = res.dislikes;
        resena.votoActual = res.votoActual;
      }
    });
  }

  toggleComentarios(resena: Resena) {
    const id = resena.id;
    this.comentariosVisibles[id] = !this.comentariosVisibles[id];

    if (this.comentariosVisibles[id] && !this.comentarios[id]) {
      this.socialService.listarComentarios(id).subscribe({
        next: (data) => (this.comentarios[id] = data),
        error: () => (this.comentarios[id] = [])
      });
    }
  }

  enviarComentario(resenaId: number) {
    const userId = this.authService.obtenerUserId();
    if (!userId) {
      alert('Inicia sesión para comentar');
      return;
    }

    const texto = this.textoComentario[resenaId]?.trim();
    if (!texto) return;

    this.socialService.crearComentario({ resenaId, userId, comentario: texto }).subscribe({
      next: (comentario) => {
        if (!this.comentarios[resenaId]) {
          this.comentarios[resenaId] = [];
        }
        this.comentarios[resenaId].push(comentario);
        this.textoComentario[resenaId] = '';
        this.comentariosVisibles[resenaId] = true;
      },
      error: () => alert('No se pudo publicar el comentario')
    });
  }

  get titulo(): string {
    return this.pelicula?.titulo || this.serie?.titulo || '';
  }

  get descripcion(): string {
    return this.pelicula?.descripcion || this.serie?.descripcion || '';
  }

  get genero(): string {
    return this.pelicula?.genero || this.serie?.genero || '';
  }

  get anio(): number {
    return this.pelicula?.anio || this.serie?.anio || 0;
  }

  get imagenUrl(): string {
    return this.pelicula?.imagenUrl || this.serie?.imagenUrl || '';
  }
}