import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin, Subscription } from 'rxjs';
import { DICA_ATUALIZAR_LISTA } from '../../core/constantes/mensagens';
import { environment } from '../../core/env/environment';
import { Cliente, Livro, Venda } from '../../core/models/models';
import { mensagemApiErro } from '../../core/services/api-error';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmDialog } from '../../shared/confirm-dialog/confirm-dialog';

type VendaDetalhada = Venda & { clienteNome?: string; livroTitulo?: string };
interface ConfirmacaoVenda { cliente: Cliente; livro: Livro; }

@Component({
  selector: 'app-vendas',
  standalone: true,
  imports: [FormsModule, ConfirmDialog],
  templateUrl: './vendas.html',
})
export class Vendas implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly cdr = inject(ChangeDetectorRef);
  readonly auth = inject(AuthService);
  private requisicaoAtualizacao?: Subscription;

  readonly formasPagamento = [
    'DINHEIRO',
    'PIX',
    'CARTAO_CREDITO',
    'CARTAO_DEBITO',
    'BOLETO',
    'TRANSFERENCIA',
  ];

  clientes: Cliente[] = [];
  livros: Livro[] = [];
  vendas: VendaDetalhada[] = [];
  clienteId: number | null = null;
  livroId: number | null = null;
  formaPagamento = 'DINHEIRO';
  carregando = true;
  salvando = false;
  vendaEmReembolso: VendaDetalhada | null = null;
  motivoReembolso = '';
  reembolsando = false;
  confirmacaoVenda: ConfirmacaoVenda | null = null;
  erro = '';
  sucesso = '';

  ngOnInit(): void { this.carregar(); }

  carregar(preservarSucesso = false): void {
    this.requisicaoAtualizacao?.unsubscribe();
    this.carregando = true;
    this.erro = '';
    if (!preservarSucesso) this.sucesso = '';
    this.requisicaoAtualizacao = forkJoin({
      clientes: this.http.get<Cliente[]>(`${environment.apiUrl}/clientes`),
      livros: this.http.get<Livro[]>(`${environment.apiUrl}/livros`),
      vendas: this.http.get<VendaDetalhada[]>(`${environment.apiUrl}/vendas`),
    }).pipe(
      finalize(() => { this.carregando = false; this.cdr.detectChanges(); }),
    ).subscribe({
      next: (dados) => {
        this.clientes = dados.clientes.filter(c => c.ativo);
        this.livros = dados.livros.filter(l => l.ativo && l.status === 'DISPONIVEL');
        this.vendas = dados.vendas;
        if (!this.clientes.some(c => c.id === this.clienteId)) this.clienteId = null;
        if (!this.livros.some(l => l.id === this.livroId)) this.livroId = null;

        const selecionada = this.vendaEmReembolso;
        if (selecionada && !dados.vendas.some(v => v.id === selecionada.id && v.status === 'REALIZADA')) {
          this.vendaEmReembolso = null;
          this.motivoReembolso = '';
        }
      },
      error: (e: HttpErrorResponse) => {
        this.erro = mensagemApiErro(e, 'Não foi possível carregar os dados de vendas.');
      },
    });
  }

  livroSelecionado(): Livro | undefined {
    return this.livros.find(l => l.id === this.livroId);
  }

  /** Abre a confirmação dentro do sistema; não usa mais o confirm() do navegador. */
  registrar(): void {
    if (!this.auth.isAdmin() || this.salvando) return;
    const cliente = this.clientes.find(c => c.id === this.clienteId);
    const livro = this.livroSelecionado();
    if (!cliente || !livro) {
      this.erro = 'Selecione um cliente ativo e um livro disponível.';
      return;
    }
    this.erro = '';
    this.sucesso = '';
    this.confirmacaoVenda = { cliente, livro };
  }

  cancelarVenda(): void {
    this.confirmacaoVenda = null;
  }

  confirmarVenda(): void {
    const confirmacao = this.confirmacaoVenda;
    if (!confirmacao || this.salvando || !this.auth.isAdmin()) return;

    this.salvando = true;
    this.erro = '';
    this.sucesso = '';
    this.http.post<VendaDetalhada>(`${environment.apiUrl}/vendas`, {
      clienteId: confirmacao.cliente.id,
      livroId: confirmacao.livro.id,
      formaPagamento: this.formaPagamento,
    }).pipe(
      finalize(() => { this.salvando = false; this.cdr.detectChanges(); }),
    ).subscribe({
      next: () => {
        this.confirmacaoVenda = null;
        this.clienteId = null;
        this.livroId = null;
        this.formaPagamento = 'DINHEIRO';
        this.sucesso = 'Venda registrada com sucesso. O exemplar foi marcado como VENDIDO.' + DICA_ATUALIZAR_LISTA;
      },
      error: (e: HttpErrorResponse) => {
        this.confirmacaoVenda = null;
        this.erro = mensagemApiErro(e, 'Não foi possível registrar a venda.');
      },
    });
  }

  iniciarReembolso(venda: VendaDetalhada): void {
    if (!this.auth.isAdmin() || venda.status !== 'REALIZADA' || this.reembolsando) return;
    this.vendaEmReembolso = venda;
    this.motivoReembolso = '';
    this.erro = '';
    this.sucesso = '';
  }

  cancelarReembolso(): void {
    this.vendaEmReembolso = null;
    this.motivoReembolso = '';
    this.erro = '';
  }

  confirmarReembolso(): void {
    const venda = this.vendaEmReembolso;
    if (!venda || this.reembolsando || !this.auth.isAdmin()) return;

    const idReembolso = venda.id;
    this.reembolsando = true;
    this.erro = '';
    this.sucesso = '';
    this.http.post<VendaDetalhada>(`${environment.apiUrl}/vendas/${idReembolso}/reembolso`, {
      motivo: this.motivoReembolso.trim() || null,
    }).pipe(
      finalize(() => { this.reembolsando = false; this.cdr.detectChanges(); }),
    ).subscribe({
      next: () => {
        if (this.vendaEmReembolso?.id === idReembolso) {
          this.vendaEmReembolso = null;
          this.motivoReembolso = '';
        }
        this.sucesso = 'Reembolso registrado com sucesso. O livro voltou ao acervo.' + DICA_ATUALIZAR_LISTA;
      },
      error: (e: HttpErrorResponse) => {
        this.erro = mensagemApiErro(e, 'Não foi possível registrar o reembolso.');
      },
    });
  }

  formatarPreco(valor: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
  }

  formatarData(valor: string | null | undefined): string {
    if (!valor) return '—';
    const data = new Date(valor);
    return Number.isNaN(data.getTime()) ? valor : data.toLocaleString('pt-BR');
  }

  rotuloFormaPagamento(valor: string | null | undefined): string {
    const rotulos: Record<string, string> = {
      DINHEIRO: 'Dinheiro',
      PIX: 'Pix',
      CARTAO_CREDITO: 'Cartão de crédito',
      CARTAO_DEBITO: 'Cartão de débito',
      BOLETO: 'Boleto',
      TRANSFERENCIA: 'Transferência',
    };
    return (valor && rotulos[valor]) || valor || '—';
  }
}
