# SpringBook — Frontend Angular

Fundação visual da SpringBook: login, layout, menu por perfil, dashboard e integração base com o backend Spring Boot.

## Pré-requisitos

- Node.js 20.19+ (ambiente atual: Node 26.10.0)
- npm 10+
- Backend Spring Boot rodando em `http://localhost:8080`

## Instalação e execução

```powershell
cd C:\Users\henri\Downloads\livros\PI-Sebo\frontend
npm install
npm start
```

Acesse **http://localhost:4200**.

Logins de desenvolvimento (já ajustados no H2):

- `admin@sebo.com` / `password` → perfil ADMIN
- `teste@sebo.com` / `password` → perfil FUNCIONARIO

> `npm start` usa `proxy.conf.json`: chamadas para `/api` são encaminhadas ao backend em `:8080`. Isso mantém a requisição na mesma origem do navegador e permite o cookie `JSESSIONID` funcionar sem alterar o CORS do Spring.

## Build de produção

```powershell
npm run build
```

Os arquivos saem em `dist/sebo-frontend/browser`.

## Estrutura

```
src/app/
├── core/
│   ├── env/environment.ts            # URL base da API
│   ├── guards/                       # authGuard e adminGuard
│   ├── interceptors/                 # envia withCredentials
│   ├── models/models.ts              # DTOs do backend em TypeScript
│   └── services/auth.service.ts      # login, logout, usuário logado
├── layout/shell/                     # sidebar + topbar
└── features/
    ├── login/                        # tela de login
    ├── dashboard/                    # painel inicial
    ├── fornecedores/                 # listagem (já integrada)
    ├── usuarios/                     # listagem (ADMIN, já integrada)
    └── em-breve/                     # placeholders dos módulos dos colegas
```

## Temas e estilo

Tokens visuais (paleta de papel/tinta, verde-musgo, bronze, fontes, botões, badges, tabelas) ficam em `src/styles.css`. Os módulos novos devem reutilizar essas classes.

## Convenções

- Componentes standalone: `nome.ts`, `nome.html`, `nome.css`.
- Não escrever `localhost:8080` no código — usar `environment.apiUrl`.
- Rotas são alteradas apenas pela Pessoa 1.
- Contratos de endpoints/DTOs: veja `..\CONTRATO-INTEGRACAO.md`.
