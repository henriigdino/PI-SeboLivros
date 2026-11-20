/** Modelos compartilhados — espelham os DTOs do backend Spring Boot. */

export type Perfil = 'ADMIN' | 'FUNCIONARIO';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  id: number;
  nome: string;
  email: string;
  tipo: Perfil;
}

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  tipo: Perfil;
  ativo: boolean;
}

export interface Fornecedor {
  id: number;
  nome: string;
  documento: string;
  email?: string;
  telefone?: string;
  endereco?: string;
  ativo: boolean;
}

/** Contrato reservado para o módulo da Pessoa 2. */
export interface Categoria {
  id: number;
  nome: string;
  descricao?: string;
  ativo: boolean;
}

/**
 * Contrato reservado para a Pessoa 3.
 * A foto é servida por GET /api/livros/{id}/foto (BLOB no H2), não vem no JSON.
 */
export interface Livro {
  id: number;
  titulo: string;
  autor: string;
  isbn?: string;
  editora?: string;
  ano?: number;
  categoriaId: number;
  categoriaNome?: string;
  preco: number;
  status: string;
  ativo: boolean;
}

/** Contrato reservado para a Pessoa 3. */
export interface Cliente {
  id: number;
  nome: string;
  tipoPessoa: 'FISICA' | 'JURIDICA';
  documento: string;
  rg?: string;
  email?: string;
  telefone?: string;
  cep?: string;
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  uf?: string;
  ativo: boolean;
}

/**
 * Contrato reservado para a Pessoa 3.
 * Modelo sugerido: um livro por venda (decisão final ainda pendente).
 */
export interface Venda {
  id: number;
  data: string;
  clienteId: number;
  livroId: number;
  valor: number;
  status: string;
}
