import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Pelicula {
  id: number;
  titulo: string;
  imagenUrl: string;
  descripcion: string;
  genero: string;
  anio: number;
}

export interface Serie {
  id: number;
  titulo: string;
  imagenUrl: string;
  descripcion: string;
  genero: string;
  anio: number;
  temporadas: number;
}

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private peliculasApi = 'https://peliculasweb-backend.onrender.com/api/peliculas';
  private seriesApi = 'https://peliculasweb-backend.onrender.com/api/series';

  constructor(private http: HttpClient) {}

  listarPeliculas(): Observable<Pelicula[]> {
    return this.http.get<Pelicula[]>(this.peliculasApi);
  }

  listarSeries(): Observable<Serie[]> {
    return this.http.get<Serie[]>(this.seriesApi);
  }
}
