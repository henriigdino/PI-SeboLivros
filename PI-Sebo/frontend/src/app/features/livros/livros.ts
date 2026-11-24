import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, Subscription } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { DICA_ATUALIZAR_LISTA } from '../../core/constantes/mensagens';
import { environment } from '../../core/env/environment';
import { Categoria, Livro } from '../../core/models/models';
import { mensagemApiErro } from '../../core/services/api-error';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmDialog } from '../../shared/confirm-dialog/confirm-dialog';

// O modelo compartilhado ainda não inclui a descrição, mas a API já a devolve.
type LivroComDescricao = Livro & { descricao?: string | null };

interface FormularioLivro {
  titulo: string;
  autor: string;
  isbn: string;
  editora: string;
  ano: number | null;
  categoriaId: number | null;
  preco: number | null;
  descricao: string;
}

@Component({
  selector: 'app-livros',
  standalone: true,
  imports: [FormsModule, MatTableModule, ConfirmDialog],
  templateUrl: './livros.html',
})
export class Livros implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly cdr = inject(ChangeDetectorRef);
  readonly auth = inject(AuthService);
  private requisicaoAtualizacao?: Subscription;
  readonly colunas = ['titulo', 'autor', 'categoria', 'preco', 'status', 'acoes'];

  // Upload de capas, separado do formulario de cadastro.
  arquivosCapa: Record<number, File | undefined> = {};
  capasAusentes = new Set<number>();
  versoesCapa: Record<number, number> = {};
  enviandoCapaId: number | null = null;
  removendoCapaId: number | null = null;

  livros: LivroComDescricao[] = [];
  categorias: Categoria[] = [];
  carregando = true;
  carregandoCategorias = true;
  erroCategorias = '';
  salvando = false;
  excluindoId: number | null = null;
  editandoId: number | null = null;
  confirmacaoExclusao: LivroComDescricao | null = null;
  erro = '';
  sucesso = '';
  formulario: FormularioLivro = this.formularioVazio();

  ngOnInit(): void {
    this.carregarCategorias();
    this.carregar();
  }

  carregarCategorias(): void {
    this.http.get<Categoria[]>(`${environment.apiUrl}/categorias`).pipe(
      finalize(() => { this.carregandoCategorias = false; this.cdr.detectChanges(); }),
    ).subscribe({
      next: (dados) => { this.categorias = dados.filter((categoria) => categoria.ativo); },
      error: () => {
        this.erroCategorias = 'Não foi possível carregar as categorias. Cadastre uma em Categorias.';
      },
    });
  }

  carregar(preservarSucesso = false): void {
    this.requisicaoAtualizacao?.unsubscribe();
    this.carregando = true;
    this.erro = '';
    if (!preservarSucesso) this.sucesso = '';
    this.requisicaoAtualizacao = this.http.get<LivroComDescricao[]>(`${environment.apiUrl}/livros`).pipe(
      finalize(() => { this.carregando = false; this.cdr.detectChanges(); }),
    ).subscribe({
      next: (dados) => {
        this.livros = [...dados];
        this.capasAusentes.clear();
        // Cada atualização explícita renova a URL das capas e evita cache do 404.
        for (const livro of dados) {
          this.versoesCapa[livro.id] = (this.versoesCapa[livro.id] ?? 0) + 1;
        }
      },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível carregar os livros.');
      },
    });
  }

  editar(livro: LivroComDescricao): void {
    if (!this.auth.isAdmin()) return;
    this.editandoId = livro.id;
    this.formulario = {
      titulo: livro.titulo,
      autor: livro.autor,
      isbn: livro.isbn ?? '',
      editora: livro.editora ?? '',
      ano: livro.ano ?? null,
      categoriaId: livro.categoriaId,
      preco: livro.preco,
      descricao: livro.descricao ?? '',
    };
    this.erro = '';
    this.sucesso = '';
  }

  cancelar(): void {
    this.editandoId = null;
    this.formulario = this.formularioVazio();
    this.erro = '';
  }

  salvar(): void {
    if (this.salvando || !this.auth.isAdmin()) return;

    const titulo = this.formulario.titulo.trim();
    const autor = this.formulario.autor.trim();
    const categoriaId = Number(this.formulario.categoriaId);
    const preco = Number(this.formulario.preco);
    const ano = this.formulario.ano;

    if (!titulo || !autor) {
      this.erro = 'Título e autor são obrigatórios.';
      return;
    }
    if (!Number.isInteger(categoriaId) || categoriaId < 1) {
      this.erro = 'Selecione uma categoria.';
      return;
    }
    if (!Number.isFinite(preco) || preco <= 0) {
      this.erro = 'O preço deve ser maior que zero.';
      return;
    }
    if (ano !== null && (!Number.isInteger(ano) || ano < 1450 || ano > 2100)) {
      this.erro = 'Informe um ano válido.';
      return;
    }

    const payload = {
      titulo,
      autor,
      isbn: this.formulario.isbn.trim() || null,
      editora: this.formulario.editora.trim() || null,
      ano,
      categoriaId,
      preco,
      descricao: this.formulario.descricao.trim() || null,
    };

    const id = this.editandoId;
    this.salvando = true;
    this.erro = '';
    this.sucesso = '';

    const requisicao = id !== null
      ? this.http.put<LivroComDescricao>(`${environment.apiUrl}/livros/${id}`, payload)
      : this.http.post<LivroComDescricao>(`${environment.apiUrl}/livros`, payload);

    requisicao.pipe(
      finalize(() => { this.salvando = false; this.cdr.detectChanges(); }),
    ).subscribe({
      next: () => {
        this.cancelar();
        this.sucesso = (id !== null ? 'Livro atualizado com sucesso.' : 'Livro adicionado com sucesso.') + DICA_ATUALIZAR_LISTA;
      },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível salvar o livro.');
      },
    });
  }

  pedirExclusao(livro: LivroComDescricao): void {
    if (!this.auth.isAdmin() || this.excluindoId !== null
        || this.enviandoCapaId !== null || this.removendoCapaId !== null) return;
    if (livro.status !== 'DISPONIVEL') {
      this.erro = 'Não exclua livros reservados ou vendidos.';
      return;
    }
    if (livro.possuiHistoricoVendas) {
      this.erro = 'Não é possível excluir: este livro possui histórico de vendas.';
      return;
    }
    this.confirmacaoExclusao = livro;
    this.erro = '';
  }

  cancelarExclusao(): void {
    this.confirmacaoExclusao = null;
  }

  confirmarExclusao(): void {
    const livro = this.confirmacaoExclusao;
    if (!livro || this.excluindoId !== null || this.enviandoCapaId !== null
        || this.removendoCapaId !== null || !this.auth.isAdmin()) return;

    this.excluindoId = livro.id;
    this.http.delete<void>(`${environment.apiUrl}/livros/${livro.id}`).pipe(
      finalize(() => { this.excluindoId = null; this.confirmacaoExclusao = null; this.cdr.detectChanges(); }),
    ).subscribe({
      next: () => {
        this.sucesso = 'Livro removido com sucesso.' + DICA_ATUALIZAR_LISTA;
      },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível excluir o livro.');
      },
    });
  }

  urlCapa(id: number): string {
    return `${environment.apiUrl}/livros/${id}/foto?v=${this.versoesCapa[id] ?? 0}`;
  }

  marcarCapaAusente(id: number): void {
    this.capasAusentes.add(id);
  }

  selecionarCapa(id: number, evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const arquivo = campo.files?.[0];
    delete this.arquivosCapa[id];
    this.erro = '';
    this.sucesso = '';
    if (!arquivo) return;

    if (!['image/jpeg', 'image/png', 'image/webp'].includes(arquivo.type)) {
      this.erro = 'Escolha uma capa JPG, PNG ou WebP.';
      campo.value = '';
      return;
    }
    if (arquivo.size > 1024 * 1024) {
      this.erro = 'A capa deve ter até 1 MB. Reduza a imagem antes de enviar.';
      campo.value = '';
      return;
    }
    this.arquivosCapa[id] = arquivo;
  }

  enviarCapa(id: number, campo: HTMLInputElement): void {
    if (!this.auth.isAdmin() || this.enviandoCapaId !== null) return;
    const arquivo = this.arquivosCapa[id];
    if (!arquivo) return;

    const dados = new FormData();
    dados.append('arquivo', arquivo, arquivo.name);
    this.enviandoCapaId = id;
    this.erro = '';
    this.sucesso = '';
    this.http.post<void>(`${environment.apiUrl}/livros/${id}/foto`, dados).pipe(
      finalize(() => { this.enviandoCapaId = null; this.cdr.detectChanges(); }),
    ).subscribe({
      next: () => {
        delete this.arquivosCapa[id];
        campo.value = '';
        this.capasAusentes.delete(id);
        this.versoesCapa[id] = (this.versoesCapa[id] ?? 0) + 1;
        this.sucesso = 'Capa adicionada com sucesso.';
        // A capa tem URL própria: basta renovar a versão, sem nova busca na lista.
      },
      error: (erro: HttpErrorResponse) => {
        if (erro.status === 413) {
          this.erro = 'Imagem maior que 1 MB. Reduza a capa e tente novamente.';
        } else {
          this.erro = mensagemApiErro(erro, 'Não foi possível enviar a capa.');
        }
      },
    });
  }

  removerCapa(id: number, campo: HTMLInputElement): void {
    if (!this.auth.isAdmin() || this.removendoCapaId !== null || this.enviandoCapaId !== null) return;

    this.removendoCapaId = id;
    this.erro = '';
    this.sucesso = '';
    this.http.delete<void>(`${environment.apiUrl}/livros/${id}/foto`).pipe(
      finalize(() => { this.removendoCapaId = null; this.cdr.detectChanges(); }),
    ).subscribe({
      next: () => {
        delete this.arquivosCapa[id];
        campo.value = '';
        this.capasAusentes.add(id);
        this.versoesCapa[id] = (this.versoesCapa[id] ?? 0) + 1;
        this.sucesso = 'Capa removida com sucesso.';
      },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível remover a capa.');
      },
    });
  }

  mascararIsbn(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const valor = campo.value.toUpperCase().replace(/[^0-9X-]/g, '').slice(0, 20);
    campo.value = valor;
    this.formulario.isbn = valor;
  }

  formatarPreco(valor: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
  }

  private formularioVazio(): FormularioLivro {
    return {
      titulo: '', autor: '', isbn: '', editora: '', ano: null,
      categoriaId: null, preco: null, descricao: '',
    };
  }
}
