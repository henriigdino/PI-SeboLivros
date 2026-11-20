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
        loadComponent: () => import('./features/em-breve/em-breve').then((m) => m.EmBreve),
        data: {
          titulo: 'Categorias',
          descricao: 'Organize o acervo por gênero. Este módulo estará disponível em breve.',
        },
      },
      {
        path: 'livros',
        title: 'Livros — SpringBook',
        loadComponent: () => import('./features/em-breve/em-breve').then((m) => m.EmBreve),
        data: {
          titulo: 'Livros',
          descricao: 'Cadastre e acompanhe cada exemplar do acervo. Disponível em breve.',
        },
      },
      {
        path: 'clientes',
        title: 'Clientes — SpringBook',
        loadComponent: () => import('./features/em-breve/em-breve').then((m) => m.EmBreve),
        data: {
          titulo: 'Clientes',
          descricao: 'Gerencie os leitores da SpringBook. Disponível em breve.',
        },
      },
      {
        path: 'vendas',
        title: 'Vendas — SpringBook',
        loadComponent: () => import('./features/em-breve/em-breve').then((m) => m.EmBreve),
        data: {
          titulo: 'Vendas',
          descricao: 'Registre vendas e mantenha o acervo atualizado. Disponível em breve.',
        },
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
