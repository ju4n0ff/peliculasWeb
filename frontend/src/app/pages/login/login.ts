import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterModule],
  templateUrl: './login.html'
})
export class LoginComponent {

  email = '';
  contrasena = '';

  constructor(private authService: AuthService, private router: Router) {}

  login() {
    this.authService.login({
      email: this.email,
      contrasena: this.contrasena
    }).subscribe({
next: (res) => {
        this.authService.guardarToken(res.token);
        if (res.userId && res.nombreUsuario && res.rol) {
          this.authService.guardarSesion(res.userId, res.nombreUsuario, res.rol);
        }
        alert('Login exitoso');
        this.router.navigate(['/perfil']);
      },
      error: (err) => {
        const mensaje = typeof err.error === 'string' ? err.error : 'Error en login';
        alert(mensaje);
      }
    });
  }
}
