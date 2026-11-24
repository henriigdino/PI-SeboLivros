import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { environment } from '../../core/env/environment';
import { Categoria, Cliente, Fornecedor, Livro, Venda } from '../../core/models/models';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly auth = inject(AuthService);
  totalFornecedores: number | null = null;
  totalCategorias: number | null = null;
  totalLivros: number | null = null;
  totalClientes: number | null = null;
  totalVendas: number | null = null;

  get saudacao(): string {
    const hora = new Date().getHours();
    if (hora < 12) return 'Bom dia';
    if (hora < 18) return 'Boa tarde';
    return 'Boa noite';
  }

  ngOnInit(): void {
    const api = environment.apiUrl;
    forkJoin({
      fornecedores: this.http.get<Fornecedor[]>(`${api}/fornecedores`)
        .pipe(catchError(() => of([]))),
      categorias: this.http.get<Categoria[]>(`${api}/categorias`)
        .pipe(catchError(() => of([]))),
      livros: this.http.get<Livro[]>(`${api}/livros`)
        .pipe(catchError(() => of([]))),
      clientes: this.http.get<Cliente[]>(`${api}/clientes`)
        .pipe(catchError(() => of([]))),
      vendas: this.http.get<Venda[]>(`${api}/vendas`)
        .pipe(catchError(() => of([]))),
    }).subscribe(({ fornecedores, categorias, livros, clientes, vendas }) => {
      this.totalFornecedores = fornecedores.length;
      this.totalCategorias = categorias.filter((categoria) => categoria.ativo).length;
      this.totalLivros = livros.length;
      this.totalClientes = clientes.filter((cliente) => cliente.ativo).length;
      this.totalVendas = vendas.filter((venda) => venda.status === 'REALIZADA').length;
      this.cdr.detectChanges();
    });
  }
}
