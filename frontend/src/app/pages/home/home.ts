import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { CatalogoService, Pelicula, Serie } from '../../services/catalogo';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class HomeComponent implements OnInit, OnDestroy {
  peliculas: Pelicula[] = [];
  series: Serie[] = [];
  videoPlaying = false;
  thumbnailError = false;
  videoSrc: SafeResourceUrl = '';
  private player: any;

  constructor(
    private catalogoService: CatalogoService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit() {
    this.videoSrc = this.sanitizer.bypassSecurityTrustResourceUrl(
      'https://www.youtube.com/embed/rQvIR1oL1vE?autoplay=1&controls=1&showinfo=0&modestbranding=1&rel=0'
    );

    this.catalogoService.listarPeliculas().subscribe({
      next: (data) => (this.peliculas = data),
      error: () => (this.peliculas = [])
    });

    this.catalogoService.listarSeries().subscribe({
      next: (data) => (this.series = data),
      error: () => (this.series = [])
    });
  }

  ngOnDestroy() {
    if (this.player && this.player.destroy) {
      this.player.destroy();
    }
  }

  playVideo(): void {
    this.videoPlaying = true;
  }

  slugify(text: string): string {
    return text.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '');
  }

  getDetalleLink(tipo: string, id: number, titulo: string): string[] {
    return ['/detalle', tipo, `${id}-${this.slugify(titulo)}`];
  }
}