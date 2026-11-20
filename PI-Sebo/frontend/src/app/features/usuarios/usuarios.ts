import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { environment } from '../../core/env/environment';
import { Usuario } from '../../core/models/models';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [MatTableModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class Usuarios implements OnInit {
  private readonly http = inject(HttpClient);

  readonly colunas = ['nome', 'email', 'tipo', 'ativo'];

  usuarios: Usuario[] = [];
  erro = '';
  carregando = true;

  ngOnInit(): void {
    this.http.get<Usuario[]>(`${environment.apiUrl}/usuarios`).subscribe({
      next: (dados) => {
        this.usuarios = dados;
        this.carregando = false;
      },
      error: () => {
        this.erro = 'Não foi possível carregar os usuários.';
        this.carregando = false;
      },
    });
  }
}
