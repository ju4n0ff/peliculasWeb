import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export type TipoContenido = 'PELICULA' | 'SERIE';

export interface Resena {
  id: number;
  userId: string;
  nombreUsuario: string;
  tipoContenido: TipoContenido;
  contenidoId: number;
  titulo: string;
  comentario: string;
  fechaCreacion: string;
  likes: number;
  dislikes: number;
  votoActual: string | null;
}

export interface CalificacionResumen {
  promedio: number;
  total: number;
  miPuntuacion: number | null;
}

export interface LikeResponse {
  likes: number;
  dislikes: number;
  votoActual: string | null;
}

export interface Comentario {
  id: number;
  resenaId: number;
  userId: string;
  nombreUsuario: string;
  comentario: string;
  fechaCreacion: string;
}

@Injectable({ providedIn: 'root' })
export class SocialService {
  private api = 'https://peliculasweb-production.up.railway.app/api/social';

  constructor(private http: HttpClient) {}

  listarResenas(tipoContenido: TipoContenido, contenidoId: number, userId?: string): Observable<Resena[]> {
    let params = new HttpParams()
      .set('tipoContenido', tipoContenido)
      .set('contenidoId', contenidoId);

    if (userId) {
      params = params.set('userId', userId);
    }

    return this.http.get<Resena[]>(`${this.api}/resenas`, { params });
  }

  votarResena(resenaId: number, userId: string, tipoVoto: 'LIKE' | 'DISLIKE'): Observable<LikeResponse> {
    const params = new HttpParams()
      .set('userId', userId)
      .set('tipoVoto', tipoVoto);

    return this.http.post<LikeResponse>(`${this.api}/resenas/${resenaId}/voto`, {}, { params });
  }

  crearResena(payload: {
    userId: string;
    tipoContenido: TipoContenido;
    contenidoId: number;
    titulo: string;
    comentario: string;
  }): Observable<Resena> {
    return this.http.post<Resena>(`${this.api}/resenas`, payload);
  }

  listarResenasUsuario(userId: string): Observable<Resena[]> {
    return this.http.get<Resena[]>(`${this.api}/resenas/usuario/${userId}`);
  }

  calificar(payload: {
    userId: string;
    tipoContenido: TipoContenido;
    contenidoId: number;
    puntuacion: number;
  }): Observable<CalificacionResumen> {
    return this.http.post<CalificacionResumen>(`${this.api}/calificaciones`, payload);
  }

  obtenerResumen(
    tipoContenido: TipoContenido,
    contenidoId: number,
    userId?: string
  ): Observable<CalificacionResumen> {
    let params = new HttpParams()
      .set('tipoContenido', tipoContenido)
      .set('contenidoId', contenidoId);

    if (userId) {
      params = params.set('userId', userId);
    }

    return this.http.get<CalificacionResumen>(`${this.api}/calificaciones/resumen`, { params });
  }

  toggleFavorito(payload: {
    userId: string;
    tipoContenido: TipoContenido;
    contenidoId: number;
  }): Observable<{ favorito: boolean }> {
    return this.http.post<{ favorito: boolean }>(`${this.api}/favoritos/toggle`, payload);
  }

  esFavorito(userId: string, tipoContenido: TipoContenido, contenidoId: number): Observable<{ favorito: boolean }> {
    const params = new HttpParams()
      .set('userId', userId)
      .set('tipoContenido', tipoContenido)
      .set('contenidoId', contenidoId);
    return this.http.get<{ favorito: boolean }>(`${this.api}/favoritos/existe`, { params });
  }

  crearComentario(payload: { resenaId: number; userId: string; comentario: string }): Observable<Comentario> {
    return this.http.post<Comentario>(`${this.api}/resenas/comentarios`, payload);
  }

  listarComentarios(resenaId: number): Observable<Comentario[]> {
    return this.http.get<Comentario[]>(`${this.api}/resenas/${resenaId}/comentarios`);
  }

  listarFavoritos(userId: string, tipoContenido?: TipoContenido): Observable<Array<{ id: number; tipoContenido: TipoContenido; contenidoId: number }>> {
    let params = new HttpParams().set('userId', userId);
    if (tipoContenido) {
      params = params.set('tipoContenido', tipoContenido);
    }
    return this.http.get<Array<{ id: number; tipoContenido: TipoContenido; contenidoId: number }>>(`${this.api}/favoritos`, { params });
  }
}
