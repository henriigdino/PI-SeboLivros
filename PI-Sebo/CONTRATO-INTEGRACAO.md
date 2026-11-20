# Contrato de integração — PI SpringBook

Este documento fixa os acordos mínimos para que as três pessoas trabalhem no **mesmo sistema** sem produzir módulos isolados.

## 1. Localização dos projetos

| Projeto | Caminho |
|---|---|
| Backend (Spring Boot — já existente) | `C:\Users\henri\Downloads\livros\livros` |
| Frontend (Angular — criado nesta etapa) | `C:\Users\henri\Downloads\livros\PI-Sebo\frontend` |
| Contrato | `C:\Users\henri\Downloads\livros\PI-Sebo\CONTRATO-INTEGRACAO.md` |

> O backend foi mantido no lugar, mas recebeu um ajuste de segurança: `/h2-console/**` agora exige perfil `ADMIN` e o console é desabilitado no perfil `prod`.

| Repositório GitHub | `https://github.com/henriigdino/PI-SeboLivros.git` |

## 2. Stack

- Backend: Java 17, Spring Boot 4.1.2, Maven, Spring Web MVC, Spring Data JPA, Spring Security, H2, Lombok.
- Frontend: Angular **22.3.x**, standalone components, Angular Material 22, TypeScript 6, RxJS 7.8.
- Comunicação em dev: proxy do Angular (`proxy.conf.json`) encaminha `/api` → `http://localhost:8080`. Isso evita problema de CORS com o cookie `JSESSIONID`.
- URLs em dev: front `http://localhost:4200`, back `http://localhost:8080`. Abra o console H2 por `http://localhost:4200/h2-console` (via proxy), após login ADMIN. O acesso direto em `:8080` também exige ADMIN.

## 3. Convenções do backend (seguir o padrão existente)

- Pacote raiz: `com.sebo.livros`.
- Pacotes: `config`, `controller`, `dto`, `entity`, `repository`, `service`.
- Entidades: `@Entity`, `@Table`, `@Data` (Lombok), `id: Long` com `GenerationType.IDENTITY`, campo `ativo: Boolean = true`.
- Repositórios: `JpaRepository<Entidade, Long>`.
- DTOs: `<Modulo>CadastroDTO` (entrada) e `<Modulo>RespostaDTO` (saída). **Nunca devolver senha**.
- Controllers: `@RestController`, `@RequestMapping("/api/<modulo>")`, `@CrossOrigin(origins = "http://localhost:4200")`.
- Services: concentram regras de negócio; controllers apenas orquestram e convertem DTO ↔ Entity.
- Senhas: sempre `PasswordEncoder` (BCrypt).
- Status: usar `String` com valores estáveis (`DISPONIVEL`, `VENDIDO`, `RESERVADO`) — alinhar com o front.

## 4. Endpoints

### Já existentes

| Método | Endpoint | Permissão |
|---|---|---|
| POST | `/api/login` | público |
| POST | `/api/logout` | público |
| GET / POST | `/api/usuarios` | ADMIN |
| GET / PUT / DELETE | `/api/usuarios/{id}` | ADMIN |
| GET / POST | `/api/fornecedores` | autenticado |
| PUT / DELETE | `/api/fornecedores/{id}` | ADMIN |

### A implementar — Pessoa 2 (Categorias)

| Método | Endpoint | Permissão sugerida |
|---|---|---|
| GET / POST | `/api/categorias` | autenticado / ADMIN escreve |
| GET / PUT / DELETE | `/api/categorias/{id}` | autenticado / ADMIN escreve |

DTOs:
- `CategoriaCadastroDTO { nome, descricao }`
- `CategoriaRespostaDTO { id, nome, descricao, ativo }`

### A implementar — Pessoa 3

| Módulo | Endpoints | Observações |
|---|---|---|
| Livros | `GET/POST /api/livros`, `GET/PUT/DELETE /api/livros/{id}`, `GET /api/livros/{id}/foto` | `categoriaId: Long`; foto como BLOB |
| Clientes | `GET/POST /api/clientes`, `GET/PUT/DELETE /api/clientes/{id}` | CPF/CNPJ/RG + ViaCEP |
| Vendas | `GET/POST /api/vendas`, `GET /api/vendas/{id}` | Baixa do status do livro na venda |

DTOs-base:
- `LivroCadastroDTO { titulo, autor, isbn, editora, ano, categoriaId, preco, status, descricao }`
- `ClienteCadastroDTO { nome, tipoPessoa, documento, rg, email, telefone, cep, logradouro, numero, complemento, bairro, cidade, uf }`
- `VendaCadastroDTO { clienteId, livroId, valor, data, formaPagamento }` — um livro por venda (decisão fechada).
- `VendaRespostaDTO { id, clienteId, livroId, valor, data, formaPagamento }`

## 5. Regras de permissão a acertar com a Pessoa 1

Hoje `SecurityConfig` só trata usuários e fornecedores. Os novos módulos caem em `anyRequest().authenticated()`. Para cada módulo, os colegas devem informar quais operações são só ADMIN; a Pessoa 1 aplica centralmente em `SecurityConfig`.

Sugestão inicial:

| Módulo | Consultar | Cadastrar | Editar | Excluir |
|---|---|---|---|---|
| Categorias | todos | ADMIN | ADMIN | ADMIN |
| Livros | todos | todos | ADMIN | ADMIN |
| Clientes | ADMIN | todos | ADMIN | ADMIN |
| Vendas | todos | todos | — | — |

> Esconder botões no Angular é UX; a segurança real permanece no backend.

## 6. Convenções do frontend

- Componentes standalone, arquivos `nome.ts`, `nome.html`, `nome.css`.
- Pastas:
  - `src/app/core/` → `env`, `models`, `interceptors`, `services`, `guards`
  - `src/app/layout/` → shell (menu/topbar)
  - `src/app/features/<modulo>/` → telas do módulo
- `environment.apiUrl = '/api'`; nunca espalhar `localhost:8080` pelo código.
- Modelos TypeScript compartilhados em `core/models/models.ts` — **usar os existentes**, não criar duplicados.
- `AuthService` expõe `usuario`, `logado` e `isAdmin` (signals). Use para controle visual de permissão.
- Rotas são de propriedade da Pessoa 1. Colegas entregam o componente e informam o caminho do arquivo; a Pessoa 1 troca o placeholder pela rota real.
- Tokens visuais (cores, fontes, botões, badges, tabelas) estão em `src/styles.css` — não redefinir localmente.

## 7. Decisões fixadas

- **Fotos dos livros:** BLOB no H2 (`byte[]`) + endpoint `GET /api/livros/{id}/foto`. Não enviar base64 no JSON.
- **Validações de documento:** CPF e CNPJ completos; RG genérico; Inscrição Estadual apenas para **SP, MG e RJ**.
- **ViaCEP:** chamada direta do Angular, com preenchimento manual como fallback.
- **UI:** Angular 22 + Material com tema customizado SpringBook (papel/tinta/verde vivo/bronze), tons claros, tipografia Fraunces nos títulos e animações de entrada em cascata. Respeita `prefers-reduced-motion`.
- **Vendas:** decisão fechada — **um livro por venda**. `Venda` referencia `livroId`; ao registrar, o status do exemplar passa a `VENDIDO`. Se o grupo quiser múltiplos itens depois, fazemos a migração.

## 8. Ajustes de backend

### Já executados

- `/h2-console/**` restrito a `ADMIN` no `SecurityConfig`.
- `application-prod.properties` desabilita o console H2 em produção.

### Ainda pendentes

Estes pontos foram identificados na análise e **não** foram alterados até aqui:

1. `@CrossOrigin(..., allowCredentials = "true")` — necessário se o front chamar `http://localhost:8080/api` diretamente. Hoje o proxy de dev resolve isso.
2. Endpoint `GET /api/me` — para restaurar o usuário logado após F5 sem depender de `sessionStorage`.
3. `@ControllerAdvice` — padronizar corpo de erros (401/403/404/409).
4. `@Valid` + anotações nos DTOs e uso de `existsByEmail` / `existsByDocumento`.

## 9. Checklist de entrega de cada módulo

1. Backend funcional (entity, repository, service, controller, DTOs).
2. Endpoints testados (HTTP).
3. Telas Angular na pasta `features/<modulo>`.
4. Comunicação front ↔ back funcionando.
5. Código seguindo este contrato, pronto para integração pela Pessoa 1.
