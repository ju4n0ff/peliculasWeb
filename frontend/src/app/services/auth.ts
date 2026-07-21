import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private API = 'https://peliculasweb-backend.onrender.com/api/usuarios';

  constructor(private http: HttpClient) {}

  login(data: any) {
    return this.http.post<any>(`${this.API}/login`, data);
  }

  register(usuario: any) {
    return this.http.post(`${this.API}/registro`, usuario, { responseType: 'text' });
  }

  reenviarVerificacion(email: string) {
    return this.http.post(`${this.API}/reenviar-verificacion`, { email }, { responseType: 'text' });
  }

  guardarToken(token: string) {
    localStorage.setItem('token', token);
  }

  obtenerToken() {
    return localStorage.getItem('token');
  }

  guardarSesion(userId: string, nombreUsuario: string, rol: string) {
    localStorage.setItem('userId', userId);
    localStorage.setItem('nombreUsuario', nombreUsuario);
    localStorage.setItem('rol', rol);
  }

  obtenerUserId(): string | null {
    return localStorage.getItem('userId');
  }

  cerrarSesion() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('nombreUsuario');
    localStorage.removeItem('rol');
  }

  esAdmin(): boolean {
    return localStorage.getItem('rol') === 'ADMIN';
  }

  obtenerRol(): string | null {
    return localStorage.getItem('rol');
  }

  obtenerNombreUsuario(): string | null {
    return localStorage.getItem('nombreUsuario');
  }
}
