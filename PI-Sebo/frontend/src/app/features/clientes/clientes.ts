import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, Subscription } from 'rxjs';
import { DICA_ATUALIZAR_LISTA } from '../../core/constantes/mensagens';
import { environment } from '../../core/env/environment';
import { Cliente } from '../../core/models/models';
import { mensagemApiErro } from '../../core/services/api-error';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmDialog } from '../../shared/confirm-dialog/confirm-dialog';

type ClienteDetalhado = Cliente & { inscricaoEstadual?: string | null };
type TipoPessoa = 'FISICA' | 'JURIDICA';

const UFS = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG',
  'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
];
const UFS_COM_IE = ['SP', 'MG', 'RJ'];
interface FormularioCliente {
  nome: string;
  tipoPessoa: TipoPessoa;
  documento: string;
  rg: string;
  inscricaoEstadual: string;
  email: string;
  telefone: string;
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  cidade: string;
  uf: string;
}
interface RetornoViaCep {
  erro?: boolean;
  logradouro?: string;
  complemento?: string;
  bairro?: string;
  localidade?: string;
  uf?: string;
}

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [FormsModule, ConfirmDialog],
  templateUrl: './clientes.html',
})
export class Clientes implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly cdr = inject(ChangeDetectorRef);
  readonly auth = inject(AuthService);
  readonly ufs = UFS;
  readonly ufsComIe = UFS_COM_IE;
  private requisicaoAtualizacao?: Subscription;
  clientes: ClienteDetalhado[] = [];
  formulario: FormularioCliente = this.vazio();
  carregando = true;
  salvando = false;
  consultandoCep = false;
  excluindoId: number | null = null;
  editandoId: number | null = null;
  confirmacaoDesativar: ClienteDetalhado | null = null;
  erro = '';
  sucesso = '';
  avisoEndereco = '';

  ngOnInit(): void { this.carregar(); }

  carregar(preservarSucesso = false): void {
    this.requisicaoAtualizacao?.unsubscribe();
    this.carregando = true;
    this.erro = '';
    if (!preservarSucesso) this.sucesso = '';
    this.avisoEndereco = '';
    this.requisicaoAtualizacao = this.http.get<ClienteDetalhado[]>(`${environment.apiUrl}/clientes`).pipe(
      finalize(() => { this.carregando = false; this.cdr.detectChanges(); }),
    ).subscribe({
      next: (dados) => { this.clientes = [...dados]; },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível listar os clientes.');
      },
    });
  }

  alterarTipo(): void {
    this.formulario.documento = '';
    this.formulario.rg = '';
    this.formulario.inscricaoEstadual = '';
  }

  alterarUf(): void {
    if (!this.ufsComIe.includes(this.formulario.uf)) {
      this.formulario.inscricaoEstadual = '';
    }
  }

  mascararDocumento(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const maximo = this.formulario.tipoPessoa === 'FISICA' ? 11 : 14;
    const digitos = this.digitos(campo.value).slice(0, maximo);
    const valor = this.formulario.tipoPessoa === 'FISICA'
      ? this.formatarCpf(digitos)
      : this.formatarCnpj(digitos);
    campo.value = valor;
    this.formulario.documento = valor;
  }

  mascararCep(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    campo.value = this.digitos(campo.value).slice(0, 8).replace(/^(\d{5})(\d)/, '$1-$2');
    this.formulario.cep = campo.value;
  }

  private formatarCpf(valor: string): string {
    return valor
      .replace(/^(\d{3})(\d)/, '$1.$2')
      .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
      .replace(/\.(\d{3})(\d)/, '.$1-$2');
  }

  private formatarCnpj(valor: string): string {
    return valor
      .replace(/^(\d{2})(\d)/, '$1.$2')
      .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
      .replace(/\.(\d{3})(\d)/, '$1/$2')
      .replace(/(\d{4})(\d)/, '$1-$2');
  }

  consultarCep(): void {
    if (this.consultandoCep) return;
    const cep = this.digitos(this.formulario.cep);
    if (cep.length !== 8) { this.erro = 'O CEP precisa ter 8 digitos.'; return; }
    this.consultandoCep = true;
    this.erro = '';
    this.http.get<RetornoViaCep>(`https://viacep.com.br/ws/${cep}/json/`).pipe(
      finalize(() => { this.consultandoCep = false; this.cdr.detectChanges(); }),
    ).subscribe({
      next: (resultado) => {
        if (resultado.erro) { this.erro = 'CEP não encontrado.'; return; }
        this.formulario.cep = cep;
        this.formulario.logradouro = resultado.logradouro ?? '';
        this.formulario.bairro = resultado.bairro ?? '';
        this.formulario.cidade = resultado.localidade ?? '';
        this.formulario.uf = resultado.uf ?? '';
        this.formulario.complemento = resultado.complemento ?? '';
        this.avisoEndereco = 'Endereço preenchido pelo ViaCEP. Confira antes de salvar.';
      },
      error: () => {
        this.erro = 'Não foi possível consultar o ViaCEP. Preencha o endereco manualmente.';
      },
    });
  }

  editar(cliente: ClienteDetalhado): void {
    if (!this.auth.isAdmin()) return;
    this.editandoId = cliente.id;
    this.formulario = {
      nome: cliente.nome, tipoPessoa: cliente.tipoPessoa,
      documento: cliente.documento, rg: cliente.rg ?? '',
      inscricaoEstadual: cliente.tipoPessoa === 'JURIDICA'
        && this.ufsComIe.includes(cliente.uf ?? '') ? (cliente.inscricaoEstadual ?? '') : '',
      email: cliente.email ?? '', telefone: cliente.telefone ?? '',
      cep: cliente.cep ?? '', logradouro: cliente.logradouro ?? '',
      numero: cliente.numero ?? '', complemento: cliente.complemento ?? '',
      bairro: cliente.bairro ?? '', cidade: cliente.cidade ?? '', uf: cliente.uf ?? '',
    };
    this.erro = '';
    this.sucesso = '';
    this.avisoEndereco = '';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancelar(): void {
    this.editandoId = null;
    this.formulario = this.vazio();
    this.erro = '';
  }

  salvar(): void {
    if (!this.auth.isAdmin() || this.salvando) return;
    const nome = this.formulario.nome.trim();
    const documento = this.digitos(this.formulario.documento);
    if (!nome) { this.erro = 'Nome obrigatorio.'; return; }
    if (documento.length !== (this.formulario.tipoPessoa === 'FISICA' ? 11 : 14)) {
      this.erro = this.formulario.tipoPessoa === 'FISICA' ? 'CPF deve ter 11 digitos.' : 'CNPJ deve ter 14 digitos.';
      return;
    }
    const cep = this.digitos(this.formulario.cep);
    if (cep && cep.length !== 8) { this.erro = 'CEP deve ter 8 digitos.'; return; }
    const payload = {
      ...this.formulario,
      nome,
      documento,
      cep: cep || null,
      rg: this.formulario.tipoPessoa === 'FISICA' ? (this.formulario.rg.trim() || null) : null,
      inscricaoEstadual: this.formulario.tipoPessoa === 'JURIDICA'
        ? (this.formulario.inscricaoEstadual.trim() || null) : null,
    };
    const id = this.editandoId;
    this.salvando = true;
    this.erro = '';
    this.sucesso = '';
    this.avisoEndereco = '';
    const req = id === null
      ? this.http.post<ClienteDetalhado>(`${environment.apiUrl}/clientes`, payload)
      : this.http.put<ClienteDetalhado>(`${environment.apiUrl}/clientes/${id}`, payload);

    req.pipe(
      finalize(() => { this.salvando = false; this.cdr.detectChanges(); }),
    ).subscribe({
      next: () => {
        this.cancelar();
        this.sucesso = (id === null ? 'Cliente adicionado com sucesso.' : 'Cliente atualizado com sucesso.') + DICA_ATUALIZAR_LISTA;
      },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível salvar o cliente.');
      },
    });
  }

  pedirDesativar(cliente: ClienteDetalhado): void {
    if (!this.auth.isAdmin() || this.excluindoId !== null) return;
    this.confirmacaoDesativar = cliente;
    this.erro = '';
  }

  cancelarDesativar(): void {
    this.confirmacaoDesativar = null;
  }

  confirmarDesativar(): void {
    const cliente = this.confirmacaoDesativar;
    if (!cliente || this.excluindoId !== null) return;

    this.excluindoId = cliente.id;
    this.http.delete<void>(`${environment.apiUrl}/clientes/${cliente.id}`).pipe(
      finalize(() => { this.excluindoId = null; this.confirmacaoDesativar = null; this.cdr.detectChanges(); }),
    ).subscribe({
      next: () => {
        this.sucesso = 'Cliente desativado. O histórico foi preservado.' + DICA_ATUALIZAR_LISTA;
      },
      error: (erro: HttpErrorResponse) => {
        this.erro = mensagemApiErro(erro, 'Não foi possível desativar o cliente.');
      },
    });
  }

  private digitos(valor: string): string { return (valor ?? '').replace(/\D/g, ''); }

  private vazio(): FormularioCliente {
    return { nome: '', tipoPessoa: 'FISICA', documento: '', rg: '',
      inscricaoEstadual: '', email: '', telefone: '', cep: '', logradouro: '',
      numero: '', complemento: '', bairro: '', cidade: '', uf: '' };
  }
}
