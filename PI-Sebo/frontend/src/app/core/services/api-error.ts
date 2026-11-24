import { HttpErrorResponse } from '@angular/common/http';

interface CorpoErro {
  mensagem?: string;
  campos?: Record<string, string>;
}

/** Extrai a mensagem padronizada do ControllerAdvice ou usa uma mensagem por status. */
export function mensagemApiErro(erro: HttpErrorResponse, padrao: string): string {
  const corpo = erro.error as CorpoErro | string | null;
  if (corpo && typeof corpo === 'object' && typeof corpo.mensagem === 'string' && corpo.mensagem) {
    return corpo.mensagem;
  }

  switch (erro.status) {
    case 0:
      return 'Sem conexão com a API. Verifique se o backend está aberto na porta 8080.';
    case 400:
      return 'Dados inválidos. Confira os campos informados.';
    case 401:
      return 'Sessão expirada. Entre novamente.';
    case 403:
      return 'Você não tem permissão para esta operação.';
    case 404:
      return 'Registro não encontrado.';
    case 409:
      return 'Operação não permitida: o registro já existe ou está em uso.';
    case 413:
      return 'Arquivo muito grande.';
    default:
      return padrao;
  }
}
