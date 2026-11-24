import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Protege as rotas internas usando apenas o estado local da sessão.
 *
 * Não consultamos /api/me a cada navegação: uma falha transitória nesse
 * endpoint (por exemplo, após um restart do backend) não deve jogar o usuário
 * para o login. Se a sessão realmente expirar, qualquer chamada à API retorna
 * 401 e o authInterceptor limpa o estado e redireciona.
 */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.logado() ? true : router.createUrlTree(['/login']);
};
