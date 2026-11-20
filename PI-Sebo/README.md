# PI — SpringBook

Projeto Integrador de gerenciamento da SpringBook, um sebo de livros.

## Estrutura

```
PI-Sebo/
├── README.md                  # este arquivo
├── CONTRATO-INTEGRACAO.md     # acordos de endpoints, DTOs, papéis e convenções
├── frontend/                  # Angular 22 + Material (fundação visual)
└── ..\livros\                 # backend Spring Boot (pasta "livros", fora desta)
```

O backend permanece em `C:\Users\henri\Downloads\livros\livros`.

## Como visualizar

Veja o `README.md` dentro de `frontend/`. Resumo:

```powershell
# Terminal 1 — backend
cd C:\Users\henri\Downloads\livros\livros
.\mvnw spring-boot:run

# Terminal 2 — frontend
cd C:\Users\henri\Downloads\livros\PI-Sebo\frontend
npm start
```

Abra `http://localhost:4200`.

## Divisão de trabalho

- **Pessoa 1** — fundação visual, login, layout, menu, comunicação base e integração final.
- **Pessoa 2** — módulo de Categorias (backend + telas).
- **Pessoa 3** — Livros, Clientes, Validações, ViaCEP e Vendas (backend + telas).

Regras e formatos estão em `CONTRATO-INTEGRACAO.md`.
