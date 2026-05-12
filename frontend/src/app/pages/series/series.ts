import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CatalogoService, Serie } from '../../services/catalogo';

@Component({
  selector: 'app-series',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './series.html',
  styleUrl: './series.css'
})
export class SeriesComponent implements OnInit {
  series: Serie[] = [];

  constructor(private catalogoService: CatalogoService) {}

  ngOnInit() {
    this.catalogoService.listarSeries().subscribe({
      next: (data) => (this.series = data),
      error: () => alert('No se pudo cargar el catalogo de series')
    });
  }

  getDetalleLink(id: number, titulo: string): string[] {
    const slug = titulo.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '');
    return ['/detalle', 'serie', `${id}-${slug}`];
  }
}
