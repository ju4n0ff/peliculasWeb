import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export type TipoContenido = 'PELICULA' | 'SERIE';

export interface Resena {
  id: number;
  usuarioId: number;
  nombreUsuario: string;
  titulo: string;
  comentario: string;
  fechaCreacion: string;
  likes: number;
  liked: boolean;
}

export interface CalificacionResumen {
  promedio: number;
  total: number;
  miPuntuacion: number | null;
    likes: number;
}

@Injectable({ providedIn: 'root' })
export class SocialService {
  private api = 'http://localhost:8080/api/social';

  constructor(private http: HttpClient) {}

  listarResenas(tipoContenido: TipoContenido, contenidoId: number): Observable<Resena[]> {
    const params = new HttpParams()
      .set('tipoContenido', tipoContenido)
      .set('contenidoId', contenidoId);
    return this.http.get<Resena[]>(`${this.api}/resenas`, { params });
  }
darLike(resenaId: number, usuarioId: number) {
  return this.http.post<{ likes: number; liked: boolean }>(
    `${this.api}/resenas/${resenaId}/usuario/${usuarioId}/like`,
    {}
  );
}

  crearResena(payload: {
    usuarioId: number;
    tipoContenido: TipoContenido;
    contenidoId: number;
    titulo: string;
    comentario: string;
  }): Observable<Resena> {
    return this.http.post<Resena>(`${this.api}/resenas`, payload);
  }

  listarResenasUsuario(usuarioId: number): Observable<Resena[]> {
    return this.http.get<Resena[]>(`${this.api}/resenas/usuario/${usuarioId}`);
  }

  calificar(payload: {
    usuarioId: number;
    tipoContenido: TipoContenido;
    contenidoId: number;
    puntuacion: number;
  }): Observable<CalificacionResumen> {
    return this.http.post<CalificacionResumen>(`${this.api}/calificaciones`, payload);
  }

  obtenerResumen(
    tipoContenido: TipoContenido,
    contenidoId: number,
    usuarioId?: number
  ): Observable<CalificacionResumen> {
    let params = new HttpParams()
      .set('tipoContenido', tipoContenido)
      .set('contenidoId', contenidoId);

    if (usuarioId) {
      params = params.set('usuarioId', usuarioId);
    }

    return this.http.get<CalificacionResumen>(`${this.api}/calificaciones/resumen`, { params });
  }

  toggleFavorito(payload: {
    usuarioId: number;
    tipoContenido: TipoContenido;
    contenidoId: number;
  }): Observable<{ favorito: boolean }> {
    return this.http.post<{ favorito: boolean }>(`${this.api}/favoritos/toggle`, payload);
  }

  esFavorito(usuarioId: number, tipoContenido: TipoContenido, contenidoId: number): Observable<{ favorito: boolean }> {
    const params = new HttpParams()
      .set('usuarioId', usuarioId)
      .set('tipoContenido', tipoContenido)
      .set('contenidoId', contenidoId);
    return this.http.get<{ favorito: boolean }>(`${this.api}/favoritos/existe`, { params });
  }

  listarFavoritos(usuarioId: number, tipoContenido?: TipoContenido): Observable<Array<{ id: number; tipoContenido: TipoContenido; contenidoId: number }>> {
    let params = new HttpParams().set('usuarioId', usuarioId);
    if (tipoContenido) {
      params = params.set('tipoContenido', tipoContenido);
    }
    return this.http.get<Array<{ id: number; tipoContenido: TipoContenido; contenidoId: number }>>(`${this.api}/favoritos`, { params });
  }
}
