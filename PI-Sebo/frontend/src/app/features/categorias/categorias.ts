import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { DICA_ATUALIZAR_LISTA } from '../../core/constantes/mensagens';
import { environment } from '../../core/env/environment';
import { Categoria } from '../../core/models/models';
import { mensagemApiErro } from '../../core/services/api-error';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmDialog } from '../../shared/confirm-dialog/confirm-dialog';

interface FormularioCategoria {
  nome: string;
  descricao: string;
}

@Component({
  selector: 'app-categorias',
  standalone: true,
  imports: [FormsModule, ConfirmDialog],
  templateUrl: './categorias.html',
})
export class Categorias implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly cdr = inject(ChangeDetectorRef);
  readonly auth = inject(AuthService);

  categorias: Categoria[] = [];
  formulario: FormularioCategoria = this.vazio();
  carregando = false;
  salvando = false;
  excluindoId: number | null = null;
  editandoId: number | null = null;
  confirmacaoExclusao: Categoria | null = null;
  erro = '';
  sucesso = '';

  ngOnInit(): void {
    this.carregar();
  }

  carregar(preservarSucesso = false): void {
    if (this.carregando) return;
    this.carregando = true;
    this.erro = '';
    if (!preservarSucesso) this.sucesso = '';
    this.http.get<Categoria[]>(`${environment.apiUrl}/categorias`).pipe(
      finalize(() => { this.carregando = false; this.cdr.detectChanges(); }),
    ).subscribe({
      next: (dados) => { this.categorias = [...dados]; },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível carregar as categorias.');
      },
    });
  }

  editar(categoria: Categoria): void {
    if (!this.auth.isAdmin()) return;
    this.editandoId = categoria.id;
    this.formulario = {
      nome: categoria.nome,
      descricao: categoria.descricao ?? '',
    };
    this.erro = '';
    this.sucesso = '';
  }

  cancelar(): void {
    this.editandoId = null;
    this.formulario = this.vazio();
    this.erro = '';
  }

  salvar(): void {
    if (this.salvando || !this.auth.isAdmin()) return;

    const nome = this.formulario.nome.trim();
    if (!nome) {
      this.erro = 'Informe o nome da categoria.';
      return;
    }

    const descricao = this.formulario.descricao.trim();
    const corpo = { nome, descricao: descricao || null };
    const url = `${environment.apiUrl}/categorias`;
    const edicao = this.editandoId !== null;

    this.salvando = true;
    this.erro = '';
    this.sucesso = '';

    const requisicao = edicao
      ? this.http.put<Categoria>(`${url}/${this.editandoId}`, corpo)
      : this.http.post<Categoria>(url, corpo);

    requisicao.pipe(
      finalize(() => { this.salvando = false; this.cdr.detectChanges(); }),
    ).subscribe({
      next: () => {
        this.editandoId = null;
        this.formulario = this.vazio();
        this.sucesso = (edicao ? 'Categoria atualizada com sucesso.' : 'Categoria salva com sucesso.') + DICA_ATUALIZAR_LISTA;
      },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível salvar a categoria.');
      },
    });
  }

  pedirExclusao(categoria: Categoria): void {
    if (!this.auth.isAdmin() || this.excluindoId !== null) return;
    this.confirmacaoExclusao = categoria;
    this.erro = '';
  }

  cancelarExclusao(): void {
    this.confirmacaoExclusao = null;
  }

  confirmarExclusao(): void {
    const categoria = this.confirmacaoExclusao;
    if (!categoria || this.excluindoId !== null) return;

    this.excluindoId = categoria.id;
    this.http.delete<void>(`${environment.apiUrl}/categorias/${categoria.id}`).pipe(
      finalize(() => { this.excluindoId = null; this.confirmacaoExclusao = null; this.cdr.detectChanges(); }),
    ).subscribe({
      next: () => {
        this.sucesso = 'Categoria excluída com sucesso.' + DICA_ATUALIZAR_LISTA;
      },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível excluir a categoria.');
      },
    });
  }

  private vazio(): FormularioCategoria {
    return { nome: '', descricao: '' };
  }
}
