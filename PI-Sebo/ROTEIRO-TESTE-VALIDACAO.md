# Roteiro de teste e validação — SpringBook

Este roteiro cobre **toda a API e todos os fluxos de tela**, incluindo a validação visual/UX final.
Execute na ordem. Marque ✅ quando o esperado acontecer e ❌ quando divergir.

## 0. Convenções

- Backend: `http://localhost:8080`
- API: `http://localhost:8080/api`
- Frontend: `http://localhost:4200` (o proxy encaminha `/api` para o backend)
- Admin seed: `admin@sebolivros.com` / `admin123`
- Use `curl.exe` (Windows) com cookie jar para manter a sessão:

```powershell
$base = 'http://localhost:8080/api'
$adm  = '.\cookie-admin.txt'
$func = '.\cookie-func.txt'

# login admin
curl.exe -s -i -c $adm -b $adm -X POST "$base/login" `
  -H "Content-Type: application/json" `
  -d '{"email":"admin@sebolivros.com","senha":"admin123"}'
```

Nos comandos seguintes, **reuse `-b $adm`**. Para testar como funcionário, use `-b $func`.

> Dica: para ver só o status HTTP, troque `-i` por `-o NUL -w "%{http_code}"`.

## 1. Preparação / reset (opcional)

Para começar do zero:

1. Pare o backend (Ctrl+C no terminal dele).
2. Apague `livros/data/sebo_db.mv.db` e `livros/data/sebo_db.trace.db` se existirem.
3. Rode `.\mvnw.cmd spring-boot:run` em `livros/`.
4. O `DataSeeder` recria o ADMIN e loga:
   `ADMIN inicial criado - e-mail: admin@sebolivros.com | senha: admin123`.

## 2. Massa de teste

| Uso | Valor |
|---|---|
| ADMIN | `admin@sebolivros.com` / `admin123` |
| FUNCIONARIO (criar na Fase 2) | `func1@sebolivros.com` / `senha123` |
| CPF válido 1 | `529.982.247-25` → `52998224725` |
| CPF válido 2 | `111.444.777-35` → `11144477735` |
| CPF inválido | `123.456.789-00` |
| CNPJ válido | `11.222.333/0001-81` → `11222333000181` |
| CNPJ inválido | `11.222.333/0001-82` |
| CEP válido | `01310-100` (São Paulo/SP) |
| IE válida (SP/MG/RJ) | `110.042.255.511` |
| UF fora da regra de IE | `BA` |
| Formas de pagamento aceitas | `DINHEIRO`, `PIX`, `CARTAO_CREDITO`, `CARTAO_DEBITO`, `BOLETO`, `TRANSFERENCIA` |

## 3. Fase 1 — Smoke, sessão e CORS

| # | Ação | Esperado |
|---|---|---|
| 1.1 | `GET /api/me` **sem** cookie | `401` (corpo pode ser vazio) |
| 1.2 | `POST /api/login` com admin | `200` + `{"id":1,"nome":"Administrador","email":"admin@sebolivros.com","tipo":"ADMIN"}` + cookie `JSESSIONID` |
| 1.3 | `GET /api/me` com `-b $adm` | `200` com o mesmo JSON |
| 1.4 | Login com senha errada | `401` |
| 1.5 | No front: entrar, dar **F5** | Continua logado; a rota usa o estado local e o `authInterceptor` só desloga se uma chamada real à API devolver `401` |
| 1.6 | No front: **Sair** e tentar abrir `/dashboard` | Redireciona para `/login` |
| 1.7 | `GET /h2-console` sem login | `401` |
| 1.8 | `GET /h2-console` com admin | `200` (tela do console) |
| 1.9 | Requisição com `Origin: http://localhost:4200` | Header `Access-Control-Allow-Origin: http://localhost:4200` e `Access-Control-Allow-Credentials: true` |

## 4. Fase 2 — Perfis e autorização

1. Crie o funcionário (admin):

```powershell
curl.exe -s -i -b $adm -X POST "$base/usuarios" `
  -H "Content-Type: application/json" `
  -d '{"nome":"Funcionario","email":"FUNC1@sebolivros.com","senha":"senha123","tipo":"funcionario"}'
```

✅ `201`; e-mail volta minúsculo; perfil volta `FUNCIONARIO`.

2. Faça login com ele:

```powershell
curl.exe -s -i -c $func -b $func -X POST "$base/login" `
  -H "Content-Type: application/json" `
  -d '{"email":"func1@sebolivros.com","senha":"senha123"}'
```

3. Com `-b $func`, confirme:

| Endpoint | Esperado |
|---|---|
| `GET /api/categorias`, `/api/livros`, `/api/clientes`, `/api/fornecedores`, `/api/vendas` | `200` |
| `GET /api/usuarios` | `403` |
| `POST /api/categorias` | `403` |
| `POST /api/clientes` | `403` (bloqueio no controller) |
| `POST /api/vendas` | `403` |
| `PUT` ou `DELETE /api/fornecedores/1` | `403` |
| `POST /api/fornecedores` | `201` — criação é permitida a qualquer autenticado |
| `POST /api/livros` | `201` — acervo é permitido a qualquer autenticado |

4. Repita as escritas com `-b $adm`: todas liberadas.
5. No front com funcionário: o avatar abre apenas os dados do funcionário e **Sair**; o link de administração de usuários não aparece e `/usuarios` redireciona.

## 5. Fase 3 — Categorias

```powershell
# criar
curl.exe -s -i -b $adm -X POST "$base/categorias" `
  -H "Content-Type: application/json" `
  -d '{"nome":"Ficcao","descricao":"Livros de ficcao"}'
```

✅ `201` — guarde o `id` (ex.: `1`).

| # | Teste | Esperado |
|---|---|---|
| 3.1 | Repetir o mesmo `nome` | `409` — `Categoria já cadastrada` |
| 3.2 | `{"nome":"","descricao":"x"}` | `400` com `campos.nome = Informe o nome da categoria` |
| 3.3 | `GET /api/categorias` | Inclui `Ficcao`, `ativo: true` |
| 3.4 | `PUT /api/categorias/1` com `{"nome":"Ficcao Nacional","descricao":"..."}` | `200` |
| 3.5 | `DELETE /api/categorias/1` **depois** de haver livro ativo nela | `409` — `Não é possível excluir: existem livros ativos nesta categoria` |
| 3.6 | Criar categoria `Teste`, sem livros, e deletar | `204`; `GET /api/categorias/{id}` → `404` |

## 6. Fase 4 — Fornecedores

```powershell
curl.exe -s -i -b $adm -X POST "$base/fornecedores" `
  -H "Content-Type: application/json" `
  -d '{"nome":"Distribuidora SP","documento":"11.222.333/0001-81","email":"contato@dist.com","telefone":"11999999999","endereco":"Rua A, 1"}'
```

✅ `201`, documento salvo como `11222333000181`.

| # | Teste | Esperado |
|---|---|---|
| 4.1 | Repetir o documento | `409` — `CPF/CNPJ já cadastrado` |
| 4.1b | Repetir e-mail | `409` — `E-mail já cadastrado` |
| 4.1c | Repetir telefone | `409` — `Telefone já cadastrado` |
| 4.2 | Documento `11.1111-11/111111` (14 dígitos) | `201` — aceito, salvo como `11111111111111` |
| 4.2b | Documento com 12 dígitos | `400` — `CPF/CNPJ invalido` |
| 4.3 | `email: "invalido"` | `400` (`campos.email`) |
| 4.4 | `PUT /api/fornecedores/1` com admin | `200` |
| 4.5 | `PUT /api/fornecedores/1` com funcionário | `403` |
| 4.6 | `DELETE /api/fornecedores/1` com admin | `204`; depois `GET` → `404` |
| 4.7 | `DELETE` com funcionário | `403` |

## 7. Fase 5 — Clientes

### 7.1 Pessoa física

```powershell
curl.exe -s -i -b $adm -X POST "$base/clientes" `
  -H "Content-Type: application/json" `
  -d '{"nome":"Ana Silva","tipoPessoa":"FISICA","documento":"529.982.247-25","rg":"12.345.678-9","cep":"01310-100","logradouro":"Av. Paulista","numero":"1000","bairro":"Bela Vista","cidade":"Sao Paulo","uf":"SP","email":"ana@teste.com","telefone":"11988887777"}'
```

✅ `201`, `documento` sem pontuação, `ativo: true`. Guarde o `id` (ex.: `1`).

### 7.2 Pessoa jurídica

```powershell
curl.exe -s -i -b $adm -X POST "$base/clientes" `
  -H "Content-Type: application/json" `
  -d '{"nome":"Livraria Nova Ltda","tipoPessoa":"JURIDICA","documento":"069.905.900/0001-23","inscricaoEstadual":"110.042.255.511","cep":"01310-100","cidade":"Sao Paulo","uf":"SP"}'
```

✅ `201`.

### 7.3 Regras de validação

| # | Teste | Esperado |
|---|---|---|
| 5.1 | `tipoPessoa` ausente/nulo | `400` — `Tipo de pessoa obrigatorio ou invalido` |
| 5.2 | `tipoPessoa: "FISICAXXX"` | `400` — `Tipo de pessoa invalido` (regex ancorada) |
| 5.3 | Documento com 10 dígitos com `FISICA` | `400` — `CPF ou CNPJ invalido` |
| 5.3b | `11.1111-11/111111` (14 dígitos) com `FISICA` ou `JURIDICA` | `201` — aceito |
| 5.4 | `rg: "abc"` com `FISICA` | `400` — `Formato de RG invalido` |
| 5.5 | `cep: "1234567"` | `400` — `CEP deve ter 8 digitos` |
| 5.6 | `JURIDICA` com `uf: "BA"` e IE preenchida | `400` — `Inscricao estadual permitida apenas para SP, MG e RJ` |
| 5.7 | `JURIDICA` com `inscricaoEstadual: "ISENTO"` e `uf: "BA"` | `201` |
| 5.8 | Repetir CPF `52998224725` | `409` — `CPF/CNPJ já cadastrado` |
| 5.8b | Repetir nome, RG, e-mail, telefone ou endereço | `409` — `Nome já cadastrado`, `RG já cadastrado`, `E-mail já cadastrado`, `Telefone já cadastrado` ou `Endereço já cadastrado` |
| 5.9 | `DELETE /api/clientes/1`, depois recadastrar o mesmo CPF | `204` e depois `201` **reaproveitando o mesmo `id`** (histórico preservado) |
| 5.10 | Escrita com funcionário | `403` |

### 7.4 Tela

- Máscara aplica ao digitar CPF (11 dígitos) e CNPJ (14).
- Campo **RG** aparece só para `FISICA`; **Inscrição estadual** só para `JURIDICA`.
- **UF** é `<select>`, não texto livre.
- Botão ViaCEP com `01310-100` preenche logradouro/bairro/cidade.

## 8. Fase 6 — Usuários

| # | Teste | Esperado |
|---|---|---|
| 6.1 | Criar com `ADMIN@SEBOLIVROS.COM` | `409` — `E-mail já cadastrado` (case-insensitive) |
| 6.2 | Criar com `senha: "12345"` | `400` — `Senha deve ter entre 6 e 100 caracteres` |
| 6.3 | Criar com `tipo: "USER"` | `400` — `Perfil invalido` |
| 6.4 | `PUT /api/usuarios/{idFunc}` trocando perfil para `ADMIN` e senha `nova123` | `200`; login com a nova senha funciona |
| 6.5 | `PUT` com `senha: "123"` | `400` |
| 6.6 | `DELETE /api/usuarios/1` (admin seed, enquanto for o último ADMIN ativo) | `409` — `Nao e possivel excluir o ultimo administrador do sistema` |
| 6.7 | Criar um segundo ADMIN, excluir um deles | `204` |
| 6.8 | Funcionário em `/api/usuarios` | `403` |

## 9. Fase 7 — Livros

```powershell
curl.exe -s -i -b $adm -X POST "$base/livros" `
  -H "Content-Type: application/json" `
  -d '{"titulo":"Dom Casmurro","autor":"Machado de Assis","isbn":"978-85-359-0277-6","editora":"Companhia das Letras","ano":1899,"categoriaId":1,"preco":59.90,"descricao":"Classico"}'
```

✅ `201`, `status: "DISPONIVEL"`, `ativo: true`. Guarde o `id` (ex.: `1`).

| # | Teste | Esperado |
|---|---|---|
| 7.1 | Enviar `"status":"VENDIDO"` na criação | Continua `DISPONIVEL` (o controller força) |
| 7.2 | `categoriaId: 999` | `400` — `Categoria inexistente ou inativa.` |
| 7.3 | `preco: 0` | `400` — `Preco deve ser maior que zero.` |
| 7.4 | `ano: 1400` | `400` (`campos.ano`) |
| 7.5 | `PUT /api/livros/1` alterando título/preço | `200`; status **não** muda |
| 7.6 | `POST /api/livros/1/foto` com `-F "arquivo=@capa.jpg"` | `204` |
| 7.7 | `GET /api/livros/1/foto` | `200` + `Content-Type: image/jpeg` |
| 7.8 | Upload de `.txt` | `400` — `Use JPG, PNG ou WebP.` |
| 7.9 | Upload > 1 MB | `413` — `A foto deve ter ate 1 MB.` |
| 7.10 | `DELETE /api/livros/1` **antes** de qualquer venda | `204` |
| 7.11 | Recrie outro livro para usar na Fase 8 | `201` |

## 10. Fase 8 — Vendas

```powershell
curl.exe -s -i -b $adm -X POST "$base/vendas" `
  -H "Content-Type: application/json" `
  -d '{"clienteId":1,"livroId":2,"formaPagamento":"PIX"}'
```

✅ `201`; `valor` é exatamente o preço do livro, `status: "REALIZADA"`, `data` preenchida.

| # | Teste | Esperado |
|---|---|---|
| 8.1 | `GET /api/livros/2` | `status: "VENDIDO"` |
| 8.2 | Vender o mesmo livro de novo | `409` — `Livro indisponivel ou ja vendido` |
| 8.3 | Enviar `valor` diferente do preço | `400` — `O valor informado deve corresponder ao preco do livro` |
| 8.4 | `formaPagamento: "CHEQUE"` | `400` — `Forma de pagamento invalida` |
| 8.5 | `data: "1999-01-01T00:00:00"` | `400` — `Data da venda fora do intervalo permitido` |
| 8.6 | Data mais de 5 min no futuro | `400` — mesma mensagem |
| 8.7 | Cliente inativo + livro disponível | `409` — `Cliente inativo nao pode comprar` |
| 8.8 | `clienteId: 999` | `404` — `Cliente nao encontrado` |
| 8.9 | Venda com funcionário | `403` |
| 8.10 | Tela: selecionar cliente ativo + livro disponível, confirmar | Mensagem de sucesso e linha nova na tabela |

> Regra-chave: **um exemplar por venda**. O valor nunca é aceito do cliente; é sempre o preço atual do livro.

## 11. Fase 9 — Reembolsos

```powershell
curl.exe -s -i -b $adm -X POST "$base/vendas/1/reembolso" `
  -H "Content-Type: application/json" `
  -d '{"motivo":"Arrependimento do cliente"}'
```

✅ `200`; `status: "REEMBOLSADA"`, `dataReembolso` preenchida, `motivoReembolso` gravado.

| # | Teste | Esperado |
|---|---|---|
| 9.1 | `GET /api/livros/2` | Voltou para `DISPONIVEL` |
| 9.2 | Repetir o reembolso | `409` — `Esta venda ja foi reembolsada ou cancelada` |
| 9.3 | `GET /api/vendas` | A venda **continua na lista** com status `REEMBOLSADA` |
| 9.4 | `DELETE /api/livros/2` | `409` — `Nao e possivel excluir um exemplar que possui historico de vendas` (mesmo reembolsada) |
| 9.5 | `motivo` com 301 caracteres | `400` — `Motivo muito longo (ate 300 caracteres)` |
| 9.6 | Body `{}` ou sem `motivo` | `200`, sem motivo gravado |
| 9.7 | Reembolso com funcionário | `403` |
| 9.8 | Tela: botão **Reembolsar** só em venda `REALIZADA` e só para admin | Confirmação + aviso para clicar em **Atualizar lista** |

## 12. Fase 10 — Padrão de erros

Toda exceção de regra de negócio/validação volta neste formato:

```json
{
  "status": 400,
  "erro": "Validacao falhou",
  "mensagem": "Verifique os campos informados.",
  "campos": { "nome": "Nome obrigatorio" }
}
```

Exemplo de conflito:

```json
{
  "status": 409,
  "erro": "Conflict",
  "mensagem": "E-mail já cadastrado",
  "campos": {}
}
```

| Situação | Status | Observação |
|---|---|---|
| Bean Validation | `400` | `campos` preenchido |
| JSON malformado / ID não numérico | `400` | `campos: {}` |
| Regra de negócio | `400/404/409` | `mensagem` explicita |
| Sem sessão | `401` | Corpo pode ser vazio (Spring Security) |
| Sem permissão | `403` | Corpo pode ser vazio (Spring Security) ou `ApiError` quando vem do controller |

No front, a mensagem amigável vem de `core/services/api-error.ts`; confirme que o texto da API aparece na tela.

## 13. Fase 11 — Checklist de tela (antes da parte visual)

- [ ] Login: mensagem de erro visível para senha errada.
- [ ] Recarregar página mantém sessão; "Sair" volta para login.
- [ ] Avatar do header é clicável: mostra os dados do perfil; apenas ADMIN vê o link **Administração de usuários**.
- [ ] Dashboard mostra contagens reais (categorias, livros, clientes, fornecedores, vendas) em 5 cards; não há card de Usuários.
- [ ] Categorias: criar/editar/excluir — sem **Desativar** nem **Reativar**; mensagem de conflito aparece ao tentar excluir categoria com livros ativos.
- [ ] Fornecedores: criar/editar/excluir; funcionário vê botões de edição/exclusão desabilitados.
- [ ] Clientes: máscaras, UF select, IE condicional, ViaCEP, reativação.
- [ ] Livros: formulário, upload de capa, miniatura via `/foto`, exclusão bloqueada com histórico.
- [ ] Vendas: selects só com clientes ativos e livros disponíveis; valor exibido; reembolso com motivo.
- [ ] Usuários: lista carrega ao abrir a página; **Editar** busca o registro antes de abrir o formulário; qualquer mutação mostra o aviso com `Clique em "Atualizar lista" para ver as alterações.` e **só** aparece na lista após clicar em **Atualizar lista**. Botões Editar/Excluir separados. Último ADMIN bloqueado.
- [ ] Usuários: criar/editar/excluir; último ADMIN bloqueado.
- [ ] Nenhum texto "Em breve" ou "Pendente" aparece.

## 14. Fase 12 — UX/visual (ajustes finais)

- [ ] **Sem lista lateral e sem lista de módulos no topo**: a navegação acontece somente no painel de **cards quadrados**.
- [ ] Header em `#12130F` contrastando com o branco e o verde; avatar clicável abre o menu do perfil. **Sair** fica nesse menu, em vermelho com brilho degradê no hover.
- [ ] Login: texto abaixo de `Cada livro encontra seu leitor.` é exatamente `Acervo, fornecedores, clientes e vendas em um só lugar!`; bloco `auto_stories` removido; botão **Entrar** com gradiente/brilho. Todos os botões de interação recebem o mesmo brilho no hover.
- [ ] Cada card tem uma cor distinta na borda esquerda (azul, bronze, roxo, ciano, vinho); no hover o card cresce, ilumina com a cor do módulo e o botão **Abrir módulo** fica verde arredondado em destaque.
- [ ] Fundo predominantemente **branco** e **verde** como cor de destaque.
- [ ] Ao salvar/adicionar: barra **verde** dentro do card. Ao remover/excluir: barra **vermelha** dentro do card. Nenhum texto fica preso em “Salvando...”, “Excluindo...” ou “Carregando...”.
- [ ] Nenhuma mutação atualiza a lista automaticamente: a mensagem de sucesso aparece na hora (sem carregamento visual e sem F5) e avisa para clicar em **Atualizar lista**; a tabela só muda quando o botão é clicado.
- [ ] Categorias: duplicado mostra `Categoria já cadastrada`; sucesso mostra `Salvo com Sucesso!`; exclusão abre modal `Deseja excluir...` (nunca `confirm()` do navegador); **não existe** Reativar/Desativar.
- [ ] Fornecedores: mensagens de e-mail, CPF/CNPJ e telefone repetidos.
- [ ] Clientes: mensagens de nome, RG, e-mail, telefone e endereço repetidos em pessoa física e jurídica.
- [ ] Campos de telefone aceitam no máximo **11 caracteres** (front: `maxlength="11"`; API rejeita mais que 11 com `Telefone deve ter ate 11 caracteres`).
- [ ] Livros: sem capa aparece placeholder `Sem imagem` no mesmo tamanho (72×104); com capa, a imagem ocupa esse mesmo tamanho em destaque; **sem filtro de Período/Década/Ano**; a nova capa aparece imediatamente após o envio.
- [ ] Livro com histórico de vendas (inclusive reembolsada): botão **Excluir** fica desabilitado com o aviso `Com histórico de venda — não é possível excluir`; a API devolve `possuiHistoricoVendas` no JSON. Livro sem histórico some da lista após clicar em **Atualizar lista**.
- [ ] A capa **só** pode ser alterada dentro de **Editar livro** — não há mais seletor/enviar capa na tabela. No formulário, a capa atual aparece em destaque com **Escolher arquivo** (verde), **Salvar capa** (JPG/PNG/WebP, até 1 MB) e **Remover capa** (`DELETE /api/livros/{id}/foto`); o botão **Salvar** fica afastado abaixo do aviso `Livro novo: DISPONÍVEL. O status mudará por venda ou reserva.`.
- [ ] Ao salvar ou remover capa, o usuário **não** é redirecionado para o login; a miniatura é renovada pela própria URL `/foto`, sem recarregar a lista.
- [ ] Ao clicar em **Atualizar lista**, a tabela sempre reflete o que já foi salvo (Angular 22 roda sem Zone.js: todas as telas chamam `detectChanges()` após a resposta HTTP e trocam a referência do array da tabela), sem depender de digitar em outro campo.
- [ ] Cabeçalhos das tabelas usam fonte legível e negrito, com linhas separando colunas e linhas; textos e acentos corrigidos (`Título`, `Preço`, `Ações`, `Histórico`, `disponível`, etc.).
- [ ] Vendas: confirmação em modal; a venda é registrada no ato da confirmação (sem F5); reembolso em formulário/modal na UI; o histórico só muda após clicar em **Atualizar lista**.
- [ ] Todos os módulos: lista carrega ao abrir a página; `Salvando...` sempre volta para `Salvar`; mensagens de erro/sucesso são limpas ao atualizar a lista; botões de ação separados por espaçamento.
- [ ] Em todas as telas com lista, o padrão é idêntico ao de Usuários: título à esquerda, botão **Atualizar lista** à direita, em verde, com o mesmo brilho/gradiente dos demais botões, e espaço vertical separando o card superior do card da lista. Para **FUNCIONÁRIO** o botão fica oculto: a lista carrega ao abrir a tela e ele apenas consulta.
- [ ] Categorias e Fornecedores carregam a lista ao abrir e o botão **Atualizar lista** nunca fica travado (`carregando` começa `false` e a guarda só bloqueia requisições em andamento).
- [ ] Badges usam degradê: **Administrador** em amarelo, **Funcionário** em rosa, `Ativo`/`Inativo` e demais status com seus degradês.
- [ ] Modal de confirmação não fecha ao clicar fora enquanto a requisição está em andamento.
- [ ] Login bloqueia cliques repetidos e sempre libera o botão `Entrar`.
- [ ] Fornecedores: funcionário não vê nem consegue usar o formulário/ações de escrita.
- [ ] Nenhum `window.confirm`/`alert` nativo aparece.

## 15. Fase 13 — Fechamento

```powershell
# backend
cd livros
.\mvnw.cmd -q test

# frontend
cd PI-Sebo\frontend
npm run build
```

✅ 8 testes passando e build OK.
Apague `cookie-admin.txt` e `cookie-func.txt` quando terminar.

## 16. Resultado

Copie esta linha preenchendo ao final:

```text
Fases 1 a 13: ____/13 aprovadas
Bloqueios encontrados: ________________________________
```