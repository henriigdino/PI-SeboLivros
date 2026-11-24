import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { finalize } from 'rxjs';
import { DICA_ATUALIZAR_LISTA } from '../../core/constantes/mensagens';
import { environment } from '../../core/env/environment';
import { Perfil, Usuario } from '../../core/models/models';
import { mensagemApiErro } from '../../core/services/api-error';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmDialog } from '../../shared/confirm-dialog/confirm-dialog';

interface FormularioUsuario {
  nome: string;
  email: string;
  senha: string;
  tipo: Perfil;
}

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [FormsModule, MatTableModule, ConfirmDialog],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class Usuarios implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly auth = inject(AuthService);
  readonly colunas = ['nome', 'email', 'tipo', 'ativo', 'acoes'];

  usuarios: Usuario[] = [];
  formulario: FormularioUsuario = this.vazio();
  erro = '';
  sucesso = '';
  carregando = false;
  carregandoEdicao = false;
  salvando = false;
  excluindoId: number | null = null;
  editandoId: number | null = null;
  confirmacaoExclusao: Usuario | null = null;

  ngOnInit(): void {
    this.carregar();
  }

  /** Carrega a lista já na abertura da página e também pelo botão "Atualizar lista". */
  carregar(preservarSucesso = false): void {
    if (this.carregando) return;

    this.carregando = true;
    this.erro = '';
    if (!preservarSucesso) this.sucesso = '';
    this.http.get<Usuario[]>(`${environment.apiUrl}/usuarios`).pipe(
      finalize(() => {
        this.carregando = false;
        this.cdr.detectChanges();
      }),
    ).subscribe({
      next: (dados) => {
        this.usuarios = [...dados];
        this.cdr.detectChanges();
      },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível carregar os usuários.');
      },
    });
  }

  /** Busca o registro antes de editar: a barra some somente quando o carregamento termina. */
  editar(usuario: Usuario): void {
    if (this.carregandoEdicao || this.salvando) return;

    this.editandoId = usuario.id;
    this.carregandoEdicao = true;
    this.erro = '';
    this.sucesso = '';
    this.formulario = { nome: '', email: '', senha: '', tipo: usuario.tipo };
    this.cdr.detectChanges();

    this.http.get<Usuario>(`${environment.apiUrl}/usuarios/${usuario.id}`).pipe(
      finalize(() => {
        this.carregandoEdicao = false;
        this.cdr.detectChanges();
      }),
    ).subscribe({
      next: (dados) => {
        this.editandoId = dados.id;
        this.formulario = {
          nome: dados.nome,
          email: dados.email,
          senha: '',
          tipo: dados.tipo,
        };
        this.cdr.detectChanges();
      },
      error: (erro: HttpErrorResponse) => {
        this.cancelar();
        this.erro = mensagemApiErro(erro, 'Não foi possível carregar o usuário para edição.');
      },
    });
  }

  cancelar(): void {
    this.editandoId = null;
    this.carregandoEdicao = false;
    this.formulario = this.vazio();
    this.erro = '';
  }

  salvar(): void {
    if (this.salvando || this.carregandoEdicao) return;

    const nome = this.formulario.nome.trim();
    const email = this.formulario.email.trim().toLowerCase();
    const senha = this.formulario.senha;
    if (!nome || !email) { this.erro = 'Informe nome e e-mail.'; return; }
    if (this.editandoId === null && senha.length < 6) {
      this.erro = 'A senha deve ter pelo menos 6 caracteres.';
      return;
    }
    if (this.editandoId !== null && senha && senha.length < 6) {
      this.erro = 'A nova senha deve ter pelo menos 6 caracteres.';
      return;
    }

    this.salvando = true;
    this.erro = '';
    this.sucesso = '';
    const corpo = { nome, email, tipo: this.formulario.tipo, senha: senha || null };
    const edicao = this.editandoId !== null;
    const requisicao = edicao
      ? this.http.put<Usuario>(`${environment.apiUrl}/usuarios/${this.editandoId}`, corpo)
      : this.http.post<Usuario>(`${environment.apiUrl}/usuarios`, corpo);

    requisicao.pipe(
      finalize(() => {
        this.salvando = false;
        this.cdr.detectChanges();
      }),
    ).subscribe({
      next: () => {
        this.salvando = false;
        this.cancelar();
        this.sucesso = (edicao
          ? 'Usuário atualizado com sucesso.'
          : 'Usuário adicionado com sucesso.') + DICA_ATUALIZAR_LISTA;
        this.cdr.detectChanges();
      },
      error: (erro: HttpErrorResponse) => {
        this.salvando = false;
        this.erro = mensagemApiErro(erro, 'Não foi possível salvar o usuário.');
      },
    });
  }

  pedirExclusao(usuario: Usuario): void {
    if (this.excluindoId !== null) return;
    this.confirmacaoExclusao = usuario;
    this.erro = '';
    this.sucesso = '';
  }

  cancelarExclusao(): void {
    this.confirmacaoExclusao = null;
  }

  confirmarExclusao(): void {
    const usuario = this.confirmacaoExclusao;
    if (!usuario || this.excluindoId !== null) return;

    this.excluindoId = usuario.id;
    this.http.delete<void>(`${environment.apiUrl}/usuarios/${usuario.id}`).pipe(
      finalize(() => {
        this.excluindoId = null;
        this.confirmacaoExclusao = null;
        this.cdr.detectChanges();
      }),
    ).subscribe({
      next: () => {
        this.sucesso = 'Usuário removido com sucesso.' + DICA_ATUALIZAR_LISTA;
        this.cdr.detectChanges();
      },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível excluir o usuário.');
      },
    });
  }

  private vazio(): FormularioUsuario {
    return { nome: '', email: '', senha: '', tipo: 'FUNCIONARIO' };
  }
}
