import { computed, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, map, Observable, of, tap } from 'rxjs';
import { environment } from '../env/environment';
import { LoginResponse } from '../models/models';

const CHAVE_USUARIO = 'sebo.usuario';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly _usuario = signal<LoginResponse | null>(this.restaurar());
  private ultimaValidacao = 0;

  readonly usuario = this._usuario.asReadonly();
  readonly logado = computed(() => this._usuario() !== null);
  readonly isAdmin = computed(() => this._usuario()?.tipo === 'ADMIN');

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

  /** Confirma no backend que a sessao salva no navegador ainda e valida. */
  validarSessao(force = false): Observable<boolean> {
    if (!this.logado()) return of(false);
    if (!force && Date.now() - this.ultimaValidacao < 60_000) return of(true);

    return this.http.get<LoginResponse>(`${environment.apiUrl}/me`).pipe(
      tap((usuario) => {
        this._usuario.set(usuario);
        sessionStorage.setItem(CHAVE_USUARIO, JSON.stringify(usuario));
        this.ultimaValidacao = Date.now();
      }),
      map(() => true),
      catchError(() => {
        this.limparSemRedirecionar();
        return of(false);
      }),
    );
  }

  login(email: string, senha: string) {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/login`, { email, senha })
      .pipe(
        tap((usuario) => {
          this._usuario.set(usuario);
          sessionStorage.setItem(CHAVE_USUARIO, JSON.stringify(usuario));
          this.ultimaValidacao = Date.now();
        }),
      );
  }

  logout() {
    return this.http.post(`${environment.apiUrl}/logout`, {});
  }

  /** Limpa o estado local e volta para a tela de login. */
  limpar(): void {
    this.limparSemRedirecionar();
    void this.router.navigate(['/login']);
  }

  private limparSemRedirecionar(): void {
    this._usuario.set(null);
    sessionStorage.removeItem(CHAVE_USUARIO);
    this.ultimaValidacao = 0;
  }

  private restaurar(): LoginResponse | null {
    try {
      const bruto = sessionStorage.getItem(CHAVE_USUARIO);
      if (!bruto) return null;
      const dados: unknown = JSON.parse(bruto);
      if (typeof dados === 'object' && dados !== null
          && 'id' in dados && 'email' in dados && 'tipo' in dados) {
        return dados as LoginResponse;
      }
      return null;
    } catch {
      return null;
    }
  }
}
