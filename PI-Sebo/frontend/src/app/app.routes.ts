import { Routes } from '@angular/router';
import { adminGuard } from './core/guards/admin.guard';
import { authGuard } from './core/guards/auth.guard';

/**
 * Rotas pertencem à Pessoa 1 (fundação/integração).
 * Os colegas entregam componentes em features/<modulo> e avisam o nome da
 * classe para que a rota seja trocada do placeholder pelo componente real.
 */
export const routes: Routes = [
  {
    path: 'login',
    title: 'Entrar — SpringBook',
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell/shell').then((m) => m.Shell),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        title: 'Painel — SpringBook',
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'fornecedores',
        title: 'Fornecedores — SpringBook',
        loadComponent: () => import('./features/fornecedores/fornecedores').then((m) => m.Fornecedores),
      },
      {
        path: 'usuarios',
        canActivate: [adminGuard],
        title: 'Usuários — SpringBook',
        loadComponent: () => import('./features/usuarios/usuarios').then((m) => m.Usuarios),
      },
      {
        path: 'categorias',
        title: 'Categorias — SpringBook',
        loadComponent: () => import('./features/categorias/categorias').then((m) => m.Categorias),
      },
      {
        path: 'livros',
        title: 'Livros — SpringBook',
        loadComponent: () => import('./features/livros/livros').then((m) => m.Livros),
      },
      {
        path: 'clientes',
        title: 'Clientes — SpringBook',
        loadComponent: () => import('./features/clientes/clientes').then((m) => m.Clientes),
      },
      {
        path: 'vendas',
        title: 'Vendas — SpringBook',
        loadComponent: () => import('./features/vendas/vendas').then((m) => m.Vendas),
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];



