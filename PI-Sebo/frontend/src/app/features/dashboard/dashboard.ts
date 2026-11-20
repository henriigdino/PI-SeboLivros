import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { environment } from '../../core/env/environment';
import { Fornecedor } from '../../core/models/models';
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

  readonly auth = inject(AuthService);
  totalFornecedores: number | null = null;

  ngOnInit(): void {
    this.http
      .get<Fornecedor[]>(`${environment.apiUrl}/fornecedores`)
      .subscribe({
        next: (dados) => (this.totalFornecedores = dados.length),
        error: () => (this.totalFornecedores = null),
      });
  }
}
