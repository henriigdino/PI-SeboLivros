import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  email = '';
  senha = '';
  erro = '';
  carregando = false;

  entrar(): void {
    if (!this.email.trim() || !this.senha) {
      this.erro = 'Informe seu e-mail e sua senha.';
      return;
    }

    this.carregando = true;
    this.erro = '';

    this.auth.login(this.email.trim(), this.senha).subscribe({
      next: () => {
        this.carregando = false;
        void this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.carregando = false;
        this.erro = 'E-mail ou senha inválidos. Verifique os dados e tente novamente.';
      },
    });
  }
}
