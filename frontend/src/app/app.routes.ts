import { Routes } from '@angular/router';
import { adminGuard } from './guards/admin.guard';
import { LoginComponent } from './pages/login/login';
import { HomeComponent } from './pages/home/home';
import { PeliculasComponent } from './pages/peliculas/peliculas';
import { SeriesComponent } from './pages/series/series';
import { GenerosComponent } from './pages/generos/generos';
import { ContactosComponent } from './pages/contactos/contactos';
import { NosotrosComponent } from './pages/nosotros/nosotros';
import { PerfilComponent } from './pages/perfil/perfil';
import { DetalleComponent } from './pages/detalle/detalle';
import { AdminComponent } from './pages/admin/admin';
import { TerminosComponent } from './pages/terminos/terminos';
import { PrivacidadComponent } from './pages/privacidad/privacidad';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'home', component: HomeComponent },
  { path: 'peliculas', component: PeliculasComponent },
  { path: 'series', component: SeriesComponent },
  { path: 'generos/:genero', component: GenerosComponent },
  { path: 'contactos', component: ContactosComponent },
  { path: 'nosotros', component: NosotrosComponent },
  { path: 'perfil', component: PerfilComponent },
  { path: 'admin', component: AdminComponent, canActivate: [adminGuard] },
  { path: 'detalle/:tipo/:slug', component: DetalleComponent },
  { path: 'terminos', component: TerminosComponent },
  { path: 'privacidad', component: PrivacidadComponent },
  { path: '', redirectTo: 'home', pathMatch: 'full' }
];
