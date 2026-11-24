import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
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
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);

  email = '';
  senha = '';
  erro = '';
  carregando = false;

  entrar(): void {
    if (this.carregando) return;
    if (!this.email.trim() || !this.senha) {
      this.erro = 'Informe seu e-mail e sua senha.';
      return;
    }

    this.carregando = true;
    this.erro = '';

    this.auth.login(this.email.trim(), this.senha).pipe(
      finalize(() => { this.carregando = false; this.cdr.detectChanges(); }),
    ).subscribe({
      next: () => {
        void this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.erro = 'E-mail ou senha inválidos. Verifique os dados e tente novamente.';
      },
    });
  }
}
