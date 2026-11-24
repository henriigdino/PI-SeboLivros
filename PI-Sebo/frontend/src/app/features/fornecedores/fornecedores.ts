import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { finalize } from 'rxjs';
import { DICA_ATUALIZAR_LISTA } from '../../core/constantes/mensagens';
import { environment } from '../../core/env/environment';
import { Fornecedor } from '../../core/models/models';
import { mensagemApiErro } from '../../core/services/api-error';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmDialog } from '../../shared/confirm-dialog/confirm-dialog';

interface FormularioFornecedor {
  nome: string;
  documento: string;
  email: string;
  telefone: string;
  endereco: string;
}

@Component({
  selector: 'app-fornecedores',
  standalone: true,
  imports: [FormsModule, MatTableModule, ConfirmDialog],
  templateUrl: './fornecedores.html',
  styleUrl: './fornecedores.css',
})
export class Fornecedores implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly cdr = inject(ChangeDetectorRef);
  readonly auth = inject(AuthService);
  readonly colunas = ['nome', 'documento', 'contato', 'ativo', 'acoes'];

  fornecedores: Fornecedor[] = [];
  formulario: FormularioFornecedor = this.vazio();
  carregando = false;
  salvando = false;
  excluindoId: number | null = null;
  editandoId: number | null = null;
  confirmacaoExclusao: Fornecedor | null = null;
  erro = '';
  sucesso = '';

  ngOnInit(): void { this.carregar(); }

  carregar(preservarSucesso = false): void {
    if (this.carregando) return;
    this.carregando = true;
    this.erro = '';
    if (!preservarSucesso) this.sucesso = '';
    this.http.get<Fornecedor[]>(`${environment.apiUrl}/fornecedores`).pipe(
      finalize(() => { this.carregando = false; this.cdr.detectChanges(); }),
    ).subscribe({
      next: (dados) => { this.fornecedores = [...dados]; },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível carregar os fornecedores.');
      },
    });
  }

  editar(fornecedor: Fornecedor): void {
    if (!this.auth.isAdmin()) return;
    this.editandoId = fornecedor.id;
    this.formulario = {
      nome: fornecedor.nome,
      documento: fornecedor.documento,
      email: fornecedor.email ?? '',
      telefone: fornecedor.telefone ?? '',
      endereco: fornecedor.endereco ?? '',
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
    const documento = this.digitos(this.formulario.documento);
    if (!nome) { this.erro = 'Informe o nome do fornecedor.'; return; }
    if (documento.length !== 11 && documento.length !== 14) {
      this.erro = 'Informe um CPF (11 dígitos) ou CNPJ (14 dígitos).';
      return;
    }

    this.salvando = true;
    this.erro = '';
    this.sucesso = '';
    const corpo = {
      nome,
      documento,
      email: this.formulario.email.trim() || null,
      telefone: this.formulario.telefone.trim() || null,
      endereco: this.formulario.endereco.trim() || null,
    };
    const requisicao = this.editandoId === null
      ? this.http.post<Fornecedor>(`${environment.apiUrl}/fornecedores`, corpo)
      : this.http.put<Fornecedor>(`${environment.apiUrl}/fornecedores/${this.editandoId}`, corpo);
    const edicao = this.editandoId !== null;

    requisicao.pipe(
      finalize(() => { this.salvando = false; this.cdr.detectChanges(); }),
    ).subscribe({
      next: () => {
        this.cancelar();
        this.sucesso = (edicao ? 'Fornecedor atualizado com sucesso.' : 'Fornecedor adicionado com sucesso.') + DICA_ATUALIZAR_LISTA;
      },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível salvar o fornecedor.');
      },
    });
  }

  pedirExclusao(fornecedor: Fornecedor): void {
    if (!this.auth.isAdmin() || this.excluindoId !== null) return;
    this.confirmacaoExclusao = fornecedor;
    this.erro = '';
  }

  cancelarExclusao(): void {
    this.confirmacaoExclusao = null;
  }

  confirmarExclusao(): void {
    const fornecedor = this.confirmacaoExclusao;
    if (!fornecedor || this.excluindoId !== null || !this.auth.isAdmin()) return;

    this.excluindoId = fornecedor.id;
    this.http.delete<void>(`${environment.apiUrl}/fornecedores/${fornecedor.id}`).pipe(
      finalize(() => { this.excluindoId = null; this.confirmacaoExclusao = null; this.cdr.detectChanges(); }),
    ).subscribe({
      next: () => {
        this.sucesso = 'Fornecedor removido com sucesso.' + DICA_ATUALIZAR_LISTA;
      },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível excluir o fornecedor.');
      },
    });
  }

  mascararDocumento(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const digitos = this.digitos(campo.value).slice(0, 14);
    campo.value = digitos.length === 11
      ? digitos.replace(/^(\d{3})(\d)/, '$1.$2')
          .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
          .replace(/\.(\d{3})(\d)/, '.$1-$2')
      : digitos.replace(/^(\d{2})(\d)/, '$1.$2')
          .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
          .replace(/\.(\d{3})(\d)/, '$1/$2')
          .replace(/(\d{4})(\d)/, '$1-$2');
    this.formulario.documento = campo.value;
  }

  contato(fornecedor: Fornecedor): string {
    return fornecedor.email || fornecedor.telefone || '—';
  }

  private digitos(valor: string): string { return (valor ?? '').replace(/\D/g, ''); }

  private vazio(): FormularioFornecedor {
    return { nome: '', documento: '', email: '', telefone: '', endereco: '' };
  }
}
