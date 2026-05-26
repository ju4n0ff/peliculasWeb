import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth';
import { CatalogoService, Pelicula, Serie } from '../../services/catalogo';

interface ResultadoBusqueda {
  id: number;
  titulo: string;
  tipo: 'pelicula' | 'serie';
}

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class HeaderComponent implements OnInit {
  showLoginModal = false;
  showRegisterModal = false;
  showUserMenu = false;
  isLoggedIn = false;
  userName = '';

  // Buscador
  terminoBusqueda = '';
  resultados: ResultadoBusqueda[] = [];
  showSugerencias = false;
  peliculas: Pelicula[] = [];
  series: Serie[] = [];

  // Login fields
  loginEmail = '';
  loginContrasena = '';
  loginError = '';
  mostrarBotonReenviar = false;
  reenviando = false;
  reenviarContador = 0;
  intervaloReenviar: any;

  // Register fields
  regNombres = '';
  regApellidos = '';
  regNombreUsuario = '';
  regEmail = '';
  regContrasena = '';
  regCelular = '';
  regError = '';
  regSuccess = '';
  registrando = false;

  constructor(
    private authService: AuthService, 
    private router: Router,
    private catalogoService: CatalogoService
  ) {
    this.checkLoginStatus();
  }

  ngOnInit() {
    this.catalogoService.listarPeliculas().subscribe({
      next: (data) => (this.peliculas = data),
      error: () => {}
    });

    this.catalogoService.listarSeries().subscribe({
      next: (data) => (this.series = data),
      error: () => {}
    });
  }

  checkLoginStatus() {
    const token = this.authService.obtenerToken();
    this.isLoggedIn = !!token;
    this.userName = localStorage.getItem('nombreUsuario') || '';
  }

  esAdmin(): boolean {
    return this.authService.esAdmin();
  }

  buscar() {
    if (!this.terminoBusqueda.trim()) {
      this.resultados = [];
      this.showSugerencias = false;
      return;
    }

    const termino = this.terminoBusqueda.toLowerCase();
    
    const peliculasEncontradas = this.peliculas
      .filter(p => p.titulo.toLowerCase().includes(termino))
      .map(p => ({ id: p.id, titulo: p.titulo, tipo: 'pelicula' as const }));

    const seriesEncontradas = this.series
      .filter(s => s.titulo.toLowerCase().includes(termino))
      .map(s => ({ id: s.id, titulo: s.titulo, tipo: 'serie' as const }));

    this.resultados = [...peliculasEncontradas, ...seriesEncontradas].slice(0, 8);
    this.showSugerencias = true;
  }

  irADetalle(resultado: ResultadoBusqueda) {
    this.router.navigate(['/detalle', resultado.tipo, resultado.id]);
    this.cerrarBusqueda();
  }

  ocultarSugerencias() {
    setTimeout(() => {
      this.showSugerencias = false;
    }, 200);
  }

  cerrarBusqueda() {
    this.showSugerencias = false;
    this.terminoBusqueda = '';
    this.resultados = [];
  }

  toggleUserMenu() {
    this.showUserMenu = !this.showUserMenu;
  }

  abrirLogin() {
    this.loginEmail = '';
    this.loginContrasena = '';
    this.loginError = '';
    this.mostrarBotonReenviar = false;
    this.showLoginModal = true;
  }

  abrirRegistro() {
    this.regNombres = '';
    this.regApellidos = '';
    this.regNombreUsuario = '';
    this.regEmail = '';
    this.regContrasena = '';
    this.regCelular = '';
    this.regError = '';
    this.regSuccess = '';
    this.showRegisterModal = true;
  }

  cerrarLogin() {
    this.showLoginModal = false;
    if (this.intervaloReenviar) {
      clearInterval(this.intervaloReenviar);
      this.reenviarContador = 0;
    }
  }

  cerrarRegistro() {
    this.showRegisterModal = false;
  }

  iniciarSesion() {
    if (!this.loginEmail || !this.loginContrasena) {
      this.loginError = 'Por favor completa todos los campos';
      return;
    }

    this.authService.login({
      email: this.loginEmail,
      contrasena: this.loginContrasena
    }).subscribe({
      next: (res) => {
        this.authService.guardarToken(res.token);
        if (res.userId && res.nombreUsuario && res.rol) {
          this.authService.guardarSesion(res.userId, res.nombreUsuario, res.rol);
        }
        this.isLoggedIn = true;
        this.userName = res.nombreUsuario;
        this.cerrarLogin();
        this.router.navigate(['/perfil']);
      },
error: (err) => {
        const errorMsg = typeof err.error === 'string' ? err.error : 'Email o contraseña incorrectos';
        this.loginError = errorMsg;
        if (errorMsg.includes('verificar')) {
          this.mostrarBotonReenviar = true;
        } else {
          this.mostrarBotonReenviar = false;
        }
      }
    });
  }

  reenviarVerificacion() {
    if (!this.loginEmail) {
      this.loginError = 'Ingresa tu email para reenviar la verificación';
      return;
    }

    if (this.reenviarContador > 0) {
      return;
    }

    this.reenviando = true;
    this.authService.reenviarVerificacion(this.loginEmail).subscribe({
      next: (mensaje) => {
        this.reenviando = false;
        this.loginError = mensaje;
        this.mostrarBotonReenviar = false;
      },
      error: (err) => {
        this.reenviando = false;
        const errorMsg = typeof err.error === 'string' ? err.error : 'Error al reenviar el correo';
        
        if (err.status === 429 || errorMsg.includes('rate_limit')) {
          this.reenviarContador = 25;
          this.loginError = `Espera ${this.reenviarContador} segundos para reenviar nuevamente`;
          
          if (this.intervaloReenviar) {
            clearInterval(this.intervaloReenviar);
          }
          
          this.intervaloReenviar = setInterval(() => {
            this.reenviarContador--;
            if (this.reenviarContador > 0) {
              this.loginError = `Espera ${this.reenviarContador} segundos para reenviar nuevamente`;
            } else {
              clearInterval(this.intervaloReenviar);
              this.loginError = 'Debes verificar tu correo antes de iniciar sesion';
            }
          }, 1000);
        } else {
          this.loginError = errorMsg;
        }
      }
    });
  }

  cerrarSesion() {
    this.authService.cerrarSesion();
    this.isLoggedIn = false;
    this.userName = '';
    this.router.navigate(['/home']);
  }

  registrarUsuario() {
    if (!this.regNombres || !this.regApellidos || !this.regNombreUsuario || !this.regEmail || !this.regContrasena) {
      this.regError = 'Por favor completa todos los campos obligatorios';
      return;
    }

    this.regError = '';
    this.regSuccess = '';
    this.registrando = true;

    const usuario = {
      nombres: this.regNombres,
      apellidos: this.regApellidos,
      nombreUsuario: this.regNombreUsuario,
      email: this.regEmail,
      contrasena: this.regContrasena,
      celular: this.regCelular,
      rol: 'USER'
    };

    this.authService.register(usuario).subscribe({
      next: (mensaje) => {
        this.registrando = false;
        this.regSuccess = mensaje || 'Usuario registrado. Revisa tu correo electrónico para verificar la cuenta antes de iniciar sesión.';
        this.regContrasena = '';
      },
      error: (err) => {
        this.registrando = false;
        const body = err.error;
        this.regError = typeof body === 'string' ? body : (body?.message || body?.error || JSON.stringify(body) || 'Error al registrar');
        console.error('Registro error:', err);
      }
    });
  }
}
