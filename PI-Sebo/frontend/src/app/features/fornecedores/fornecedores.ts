import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { environment } from '../../core/env/environment';
import { Fornecedor } from '../../core/models/models';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-fornecedores',
  standalone: true,
  imports: [MatTableModule],
  templateUrl: './fornecedores.html',
  styleUrl: './fornecedores.css',
})
export class Fornecedores implements OnInit {
  private readonly http = inject(HttpClient);

  readonly auth = inject(AuthService);
  readonly colunas = ['nome', 'documento', 'contato', 'ativo'];

  fornecedores: Fornecedor[] = [];
  erro = '';
  carregando = true;

  ngOnInit(): void {
    this.http.get<Fornecedor[]>(`${environment.apiUrl}/fornecedores`).subscribe({
      next: (dados) => {
        this.fornecedores = dados;
        this.carregando = false;
      },
      error: () => {
        this.erro = 'Não foi possível carregar os fornecedores.';
        this.carregando = false;
      },
    });
  }

  contato(fornecedor: Fornecedor): string {
    return fornecedor.email || fornecedor.telefone || '—';
  }
}
