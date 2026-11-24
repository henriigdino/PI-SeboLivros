import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  templateUrl: './shell.html',
  styleUrl: './shell.css',
})
export class Shell {
  readonly auth = inject(AuthService);
  menuPerfilAberto = false;

  alternarMenu(): void {
    this.menuPerfilAberto = !this.menuPerfilAberto;
  }

  fecharMenu(): void {
    this.menuPerfilAberto = false;
  }

  logout(): void {
    this.fecharMenu();
    this.auth.logout().subscribe({
      next: () => this.auth.limpar(),
      error: () => this.auth.limpar(),
    });
  }
}
