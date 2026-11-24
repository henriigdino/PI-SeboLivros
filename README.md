# PI Sebo Livros

Projeto Spring Boot + H2 (backend em `livros/`) e Angular (frontend em `PI-Sebo/frontend/`).

O repositório contém **somente o código**: cada pessoa gera o **próprio banco local** na primeira execução. Isso evita travamento do `.mv.db` e conflitos binários no Git.

## Como começar

Requisitos: **Java 21** e **Node.js** (para o frontend).

### Backend

```powershell
cd PI-SeboLivros/livros
.\mvnw.cmd spring-boot:run
```

API: `http://localhost:8080`

### Frontend

```powershell
cd PI-SeboLivros/PI-Sebo/frontend
npm install
npm start
```

Aplicação: `http://localhost:4200`

## Login inicial

| E-mail | Senha |
| --- | --- |
| `admin@sebolivros.com` | `admin123` |

O usuário é criado automaticamente pelo `DataSeeder` quando o banco está vazio.
Ele é desabilitado em produção (`application-prod.properties`) e nos testes.
Troque a senha depois de entrar.

## Console do H2

1. Entre na aplicação com o usuário administrador acima (o console exige `ROLE_ADMIN`).
2. Acesse `http://localhost:8080/h2-console`.
3. Use:
   - **JDBC URL:** `jdbc:h2:file:./data/sebo_db`
   - **User:** `sa`
   - **Password:** *(vazio)*

## Banco de dados

- O arquivo fica em `livros/data/sebo_db.mv.db` e **não deve ser commitado** (já está no `.gitignore`).
- Para “resetar” seu banco local: pare a aplicação, apague `data/sebo_db.mv.db` e rode novamente. O administrador será recriado.
- Para compartilhar mudanças de estrutura/dados com o time, commite o código (entidades, `DataSeeder`, migrações), nunca o `.mv.db`.

## Opcional: banco compartilhado via rede

Se o time quiser um único banco acessível por várias máquinas, suba um servidor H2 TCP:

```powershell
java -cp "~/.m2/repository/com/h2database/h2/2.4.240/h2-2.4.240.jar" org.h2.tools.Server -tcp -tcpAllowOthers
```

E rode o backend apontando para ele:

```powershell
$env:SEBO_DB_URL = 'jdbc:h2:tcp://IP_DA_MAQUINA:9092/./data/sebo_db'
.\mvnw.cmd spring-boot:run
```

Use apenas em rede confiável (porta 9092 liberada) e defina senha no banco antes de expor o H2.
