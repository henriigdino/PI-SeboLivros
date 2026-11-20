import { computed, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { environment } from '../env/environment';
import { LoginResponse } from '../models/models';

const CHAVE_USUARIO = 'sebo.usuario';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly _usuario = signal<LoginResponse | null>(this.restaurar());

  readonly usuario = this._usuario.asReadonly();
  readonly logado = computed(() => this._usuario() !== null);
  readonly isAdmin = computed(() => this._usuario()?.tipo === 'ADMIN');

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

  login(email: string, senha: string) {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/login`, { email, senha })
      .pipe(
        tap((usuario) => {
          this._usuario.set(usuario);
          sessionStorage.setItem(CHAVE_USUARIO, JSON.stringify(usuario));
        }),
      );
  }

  logout() {
    return this.http.post(`${environment.apiUrl}/logout`, {});
  }

  /** Limpa o estado local e volta para a tela de login. */
  limpar(): void {
    this._usuario.set(null);
    sessionStorage.removeItem(CHAVE_USUARIO);
    void this.router.navigate(['/login']);
  }

  private restaurar(): LoginResponse | null {
    try {
      const bruto = sessionStorage.getItem(CHAVE_USUARIO);
      if (!bruto) return null;
      const dados: unknown = JSON.parse(bruto);
      if (typeof dados === 'object' && dados !== null && 'tipo' in dados) {
        return dados as LoginResponse;
      }
      return null;
    } catch {
      return null;
    }
  }
}
