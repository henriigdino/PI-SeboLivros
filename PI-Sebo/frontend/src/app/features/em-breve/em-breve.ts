import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-em-breve',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './em-breve.html',
  styleUrl: './em-breve.css',
})
export class EmBreve {
  private readonly route = inject(ActivatedRoute);

  readonly titulo = String(this.route.snapshot.data['titulo'] ?? 'Módulo');
  readonly descricao = String(
    this.route.snapshot.data['descricao'] ?? 'Este módulo está sendo desenvolvido.',
  );
}
