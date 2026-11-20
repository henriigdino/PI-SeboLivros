import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../env/environment';

/**
 * Garante que as chamadas para a API enviem/recebam o cookie JSESSIONID.
 * Com o proxy de desenvolvimento a requisição é same-origin, mas manter o
 * withCredentials facilita trocar para a URL absoluta no futuro.
 */
export const credentialsInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.startsWith(environment.apiUrl)) {
    return next(req.clone({ withCredentials: true }));
  }
  return next(req);
};
