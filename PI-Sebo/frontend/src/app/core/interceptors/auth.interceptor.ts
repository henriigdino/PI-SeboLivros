import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject, Injector } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/** Sessao expirada no servidor: limpa o estado local e volta para o login. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Injector é obtido no contexto de injeção, mas o AuthService só é buscado
  // quando ocorre 401, evitando dependência circular com o HttpClient.
  const injector = inject(Injector);
  // A capa usa URL própria (inclusive em <img>). Um 401 nela não deve
  // deslogar o usuário nem redirecionar para o login.
  const ignore = req.url.includes('/api/login') || req.url.includes('/api/logout')
    || req.url.includes('/api/me') || req.url.includes('/foto');

  return next(req).pipe(
    catchError((erro: HttpErrorResponse) => {
      if (erro.status === 401 && !ignore) {
        injector.get(AuthService).limpar();
      }
      return throwError(() => erro);
    }),
  );
};
