/**
 * URL base da API.
 *
 * Em desenvolvimento usamos o proxy do Angular (proxy.conf.json), que encaminha
 * /api para http://localhost:8080. Assim o navegador enxerga a API na mesma
 * origem (:4200) e o cookie JSESSIONID funciona sem depender de ajustes de CORS.
 *
 * Para apontar direto ao backend, troque por 'http://localhost:8080/api' e
 * configure allowCredentials=true no @CrossOrigin do Spring.
 */
export const environment = {
  apiUrl: '/api',
};
