# PI Sebo Livros

Projeto Spring Boot + H2. O repositório contém **somente o código**: cada pessoa
que clonar gera o **próprio banco local** na primeira execução. Isso evita o
bloqueio do arquivo `.mv.db` e conflitos binários no Git.

## Como começar (qualquer máquina)

Requisito: **Java 21**.

```powershell
git clone https://github.com/henriigdino/PI-SeboLivros.git
cd PI-SeboLivros/livros
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

## Login inicial

| E-mail | Senha |
| --- | --- |
| `admin@sebo.com` | `admin123` |

Esse usuário é criado por `src/main/resources/data.sql` na primeira execução.
Troque a senha depois de entrar.

## Console do H2

1. Entre na aplicação com o usuário `admin` acima (o console exige `ROLE_ADMIN`).
2. Acesse `http://localhost:8080/h2-console`.
3. Use:
   - **JDBC URL:** `jdbc:h2:file:./data/sebo_db`
   - **User:** `sa`
   - **Password:** *(vazio)*

## Banco de dados

- O arquivo fica em `livros/data/sebo_db.mv.db` e **não deve ser commitado**
  (já está no `.gitignore`).
- Ao rodar, o Hibernate cria/atualiza as tabelas e o `data.sql` insere apenas
  os registros iniciais que ainda não existem.
- Para “resetar” seu banco local: pare a aplicação, apague
  `data/sebo_db.mv.db` e rode novamente. Ele será recriado a partir do `data.sql`.
- Para compartilhar mudanças de estrutura ou dados de teste com o time, edite
  as entidades ou o `data.sql` e commite — não commite o `.mv.db`.

## Opcional: banco compartilhado via rede

Se o time quiser um único banco acessível por várias máquinas, suba um servidor
H2 TCP em uma máquina:

```powershell
java -cp "~/.m2/repository/com/h2database/h2/2.4.240/h2-2.4.240.jar" org.h2.tools.Server -tcp -tcpAllowOthers
```

E rode a aplicação apontando para ele:

```powershell
$env:SEBO_DB_URL = 'jdbc:h2:tcp://IP_DA_MAQUINA:9092/./data/sebo_db'
.\mvnw.cmd spring-boot:run
```

Use apenas em rede confiável (a porta 9092 precisa estar liberada) e defina uma
senha no banco antes de expor o H2.
