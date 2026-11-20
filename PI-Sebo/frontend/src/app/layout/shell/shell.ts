import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface ItemMenu {
  path: string;
  label: string;
  icon: string;
  somenteAdmin?: boolean;
}

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.html',
  styleUrl: './shell.css',
})
export class Shell {
  readonly auth = inject(AuthService);

  private readonly itens: ItemMenu[] = [
    { path: '/dashboard', label: 'Painel', icon: 'dashboard' },
    { path: '/fornecedores', label: 'Fornecedores', icon: 'local_shipping' },
    { path: '/categorias', label: 'Categorias', icon: 'category' },
    { path: '/livros', label: 'Livros', icon: 'menu_book' },
    { path: '/clientes', label: 'Clientes', icon: 'groups' },
    { path: '/vendas', label: 'Vendas', icon: 'receipt_long' },
    { path: '/usuarios', label: 'Usuários', icon: 'manage_accounts', somenteAdmin: true },
  ];

  get menuVisivel(): ItemMenu[] {
    return this.itens.filter((item) => !item.somenteAdmin || this.auth.isAdmin());
  }

  logout(): void {
    this.auth.logout().subscribe({
      next: () => this.auth.limpar(),
      error: () => this.auth.limpar(),
    });
  }
}
