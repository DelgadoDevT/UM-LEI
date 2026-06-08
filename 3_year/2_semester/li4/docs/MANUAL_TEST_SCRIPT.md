# Manual Test Script — Taki Backend & Frontend

> **Branch:** `dev`
> **API Local (modo BRANCH / CENTRAL):** `http://<host>:8080`
> **API Global (modo CENTRAL):** `http://<host>:8081`
> **Frontend (Vite):** `http://<host>:5173`
> **Autenticação:** Cookie HttpOnly `auth_token` (JWT RS256), obtido em `POST /api/{local|global}/auth/login`
> **Formato:** JSON (`Content-Type: application/json`)
> **Pré-requisito:** Sistema instalado de acordo com [`MANUAL_INSTALACAO.md`](MANUAL_INSTALACAO.md).

---

## Índice

1.  [Pré-requisitos](#1-pré-requisitos)
2.  [Setup da Infraestrutura](#2-setup-da-infraestrutura)
3.  [Variáveis de sessão para `curl`](#3-variáveis-de-sessão-para-curl)
4.  [Credenciais de Teste](#4-credenciais-de-teste)
5.  [Smoke Tests](#5-smoke-tests)
6.  [API Local — Endpoints](#6-api-local--endpoints)
7.  [API Global — Endpoints](#7-api-global--endpoints)
8.  [Cenários E2E de Integração](#8-cenários-e2e-de-integração)
9.  [Sincronização Loja ↔ Sede](#9-sincronização-loja--sede)
10. [Testes de Interface (UI)](#10-testes-de-interface-ui)
11. [Auditoria de Segurança](#11-auditoria-de-segurança)
12. [Tabela de Erros Esperados](#12-tabela-de-erros-esperados)

---

## 1. Pré-requisitos

- Docker Engine **24.x** e Docker Compose **2.x** instalados.
- `curl`, `jq` e `python3` disponíveis no terminal.
- Repositório clonado em `dev` (`git checkout dev`).
- Chaves RSA geradas e colocadas no `.env` da raiz — ver §3 do [`MANUAL_INSTALACAO.md`](MANUAL_INSTALACAO.md).
- IP da sede registado em `lan_ip.txt` (valor atual: `192.168.1.82`).

> JDK 21 e Node.js **não** são necessários nas máquinas — o Docker trata de tudo. Só são precisos se quiseres correr o projeto fora dos containers.

---

## 2. Setup da Infraestrutura

Conforme o cenário, escolhe **um** dos três perfis Docker:

| Ficheiro | Modo | Quando usar |
|---|---|---|
| `docker-compose-central.yml` | **CENTRAL** | Servidor da sede — API Local + Global |
| `docker-compose.yml`         | **BRANCH**  | Servidor de cada loja — apenas API Local |
| `docker-compose-dev.yml`     | **DEV**     | Ambiente completo numa só máquina |

### 2.1 Servidor Central (sede)

```bash
cd /caminho/para/LI4_2526
docker compose -f docker-compose-central.yml down -v --remove-orphans
docker compose -f docker-compose-central.yml up -d --build
sleep 5
bash ./database/load_data.sh central
```

Containers esperados:

- `li4_db_central` — PostgreSQL `:5432`
- `li4_backend_central` — Javalin `:8080` (API Local) + `:8081` (API Global)
- `li4_frontend_central` — Vite `:5173`

### 2.2 Servidor de Loja (BRANCH, repetir para `N = 1..4`)

1. No `docker-compose.yml`, ajustar `TAKI_CORS_ORIGIN` para o IP do frontend da loja:
   ```yaml
   environment:
     - TAKI_CORS_ORIGIN=http://<IP_DO_FRONTEND_LOJA>:5173
   ```
2. No `.env`, definir o endereço da sede:
   ```ini
   URLS_SEDE_CSV=http://192.168.1.82:8081/api/v1
   ```
3. Arrancar e popular:
   ```bash
   docker compose -f docker-compose.yml down -v --remove-orphans
   docker compose -f docker-compose.yml up -d --build
   sleep 5
   bash ./database/load_data.sh N   # substituir N pelo número da loja (1..4)
   ```

Containers esperados:

- `li4_db_local_1` — PostgreSQL `:5433`
- `li4_backend` — Javalin `:8080` (API Local)
- `li4_frontend` — Vite `:5173`

### 2.3 Reset rápido (limpa volumes e re-popula)

```bash
docker compose -f docker-compose-central.yml down -v --remove-orphans
docker compose -f docker-compose-central.yml up -d --build && sleep 5
bash ./database/load_data.sh central
```

### 2.4 Modo de Desenvolvimento (Local Dev)

Este perfil (`docker-compose-dev.yml`) é otimizado para o dia-a-dia de desenvolvimento. Tens duas formas de o usar:

#### Opção A: Full Docker (Zero Setup)
Ideal para quem quer apenas ver o sistema a correr com as configurações de desenvolvimento sem instalar Java ou Node na máquina.

```bash
# Levanta tudo: 6 Bases de Dados + Backend + Frontend
docker compose -f docker-compose-dev.yml up -d --build
bash ./database/load_data.sh
```

- **Vantagem:** Não precisas de nada instalado exceto Docker.
- **Auto-Reload:** O Frontend (Vite) deteta alterações nos ficheiros e atualiza automaticamente. O Backend (Gradle) requer um restart do container para novas alterações de código.

#### Opção B: Híbrido (IDE & Debugging)
Ideal para quem está a escrever código e precisa de usar o **Debugger** do IntelliJ/VS Code ou quer um ciclo de feedback mais rápido.

1. **Levantar apenas as Bases de Dados:**
   ```bash
   docker compose -f docker-compose-dev.yml up -d db_central db_local_1
   ```
2. **Correr o Backend no IDE ou Terminal:**
   ```bash
   cd backend
   ./gradlew run
   ```
3. **Correr o Frontend no Terminal:**
   ```bash
   cd frontend
   npm run dev
   ```

> **Dica:** No modo híbrido, garante que o teu `.env` aponta para `localhost` (ex: `TAKI_DB_URL=jdbc:postgresql://localhost:5433/...`).

---

## 3. Variáveis de sessão para `curl`

Define no shell antes de cada bateria de testes (ajustar `<host>`):

```bash
export BASE_LOCAL=http://localhost:8080
export BASE_GLOBAL=http://localhost:8081
export COOKIES=./cookies.txt
```

> Todos os comandos a seguir usam `-c $COOKIES` (escrita do cookie no login) e `-b $COOKIES` (leitura nas chamadas autenticadas).

---

## 4. Credenciais de Teste

| Utilizador | Email | Password | Role | Loja |
|---|---|---|---|---|
| Gestor Central  | `dono@taki.pt`            | `admin` | `GESTOR_CENTRAL`  | Central |
| Gerente Loja 1  | `tiago.rocha@taki.pt`     | `admin` | `GESTOR_LOJA`     | 1 |
| Operador Loja 1 | `operador1@taki.pt`       | `admin` | `OPERADOR_CAIXA`  | 1 |
| Gerente Loja 2  | `sofia.alves@taki.pt`     | `admin` | `GESTOR_LOJA`     | 2 |
| Operador Loja 2 | `operador2@taki.pt`       | `admin` | `OPERADOR_CAIXA`  | 2 |
| Gerente Loja 3  | `carolina.jesus@taki.pt`  | `admin` | `GESTOR_LOJA`     | 3 |
| Operador Loja 3 | `operador3@taki.pt`       | `admin` | `OPERADOR_CAIXA`  | 3 |
| Gerente Loja 4  | `david.marques@taki.pt`   | `admin` | `GESTOR_LOJA`     | 4 |
| Operador Loja 4 | `operador4@taki.pt`       | `admin` | `OPERADOR_CAIXA`  | 4 |

---

## 5. Smoke Tests

Bateria mínima que **tem** de passar antes de qualquer outro cenário.

### 5.1 Health checks

```bash
curl -i $BASE_LOCAL/health        # esperado: 200 OK
curl -i $BASE_GLOBAL/health       # esperado: 200 OK   (apenas em CENTRAL)
```

### 5.2 Login do gestor central

```bash
curl -c $COOKIES -s -X POST $BASE_GLOBAL/api/global/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"dono@taki.pt","password":"admin"}' | jq .
```

✅ `200 OK` — payload com `data.funcionario { id, nome, email, idPerfilAcesso }`. Cookie `auth_token` HttpOnly criado.

### 5.3 Login do gerente da loja 1

```bash
curl -c $COOKIES -s -X POST $BASE_LOCAL/api/local/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"tiago.rocha@taki.pt","password":"admin"}' | jq .
```

✅ `200 OK` — payload semelhante.

### 5.4 Sessão atual

```bash
curl -b $COOKIES -s $BASE_LOCAL/api/local/auth/me | jq .
```

✅ `200 OK` com o funcionário atualmente autenticado.

### 5.5 Logout

```bash
curl -b $COOKIES -X POST $BASE_LOCAL/api/local/auth/logout -i
```

✅ `200 OK` e cabeçalho `Set-Cookie` que invalida `auth_token`.

---

## 6. API Local — Endpoints

Base URL: `$BASE_LOCAL = http://localhost:8080`.
Excepto `POST /auth/login`, todos os endpoints requerem o cookie `auth_token` (usar `-b $COOKIES`).

### 6.1 Autenticação

| Método | Endpoint | Acesso |
|---|---|---|
| `POST`  | `/api/local/auth/login`            | público |
| `POST`  | `/api/local/auth/logout`           | autenticado |
| `GET`   | `/api/local/auth/me`               | autenticado |
| `POST`  | `/api/local/auth/verify-password`  | autenticado |

```bash
curl -c $COOKIES -s -X POST $BASE_LOCAL/api/local/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"tiago.rocha@taki.pt","password":"admin"}' | jq .
```

### 6.2 Catálogo (produtos e categorias)

| Método | Endpoint | Role |
|---|---|---|
| `GET`    | `/api/local/produtos`                            | qualquer autenticado |
| `POST`   | `/api/local/produtos`                            | `GESTOR_LOJA` |
| `PATCH`  | `/api/local/produtos/{idProduto}`                | `GESTOR_LOJA` |
| `PATCH`  | `/api/local/produtos/{idProduto}/inativar`       | `GESTOR_LOJA` |
| `GET`    | `/api/local/categorias`                          | qualquer autenticado |
| `POST`   | `/api/local/categorias`                          | `GESTOR_LOJA` |
| `PATCH`  | `/api/local/categorias/{idCategoria}`            | `GESTOR_LOJA` |
| `PATCH`  | `/api/local/categorias/{idCategoria}/inativar`   | `GESTOR_LOJA` |

```bash
curl -b $COOKIES -s $BASE_LOCAL/api/local/produtos | jq '.data | length'
```

### 6.3 Funcionários e perfis

| Método | Endpoint | Role |
|---|---|---|
| `GET`    | `/api/local/funcionarios`                                | autenticado |
| `POST`   | `/api/local/funcionarios`                                | `GESTOR_LOJA` |
| `PATCH`  | `/api/local/funcionarios/{idFuncionario}`                | `GESTOR_LOJA` |
| `POST`   | `/api/local/funcionarios/{idFuncionario}/bloquear`       | `GESTOR_LOJA` |
| `POST`   | `/api/local/funcionarios/{idFuncionario}/desbloquear`    | `GESTOR_LOJA` |
| `PATCH`  | `/api/local/funcionarios/{idFuncionario}/perfil`         | `GESTOR_LOJA` |
| `DELETE` | `/api/local/funcionarios/{idFuncionario}`                | `GESTOR_LOJA` |
| `GET`    | `/api/local/perfis`                                      | autenticado |
| `POST`   | `/api/local/perfis`                                      | `GESTOR_LOJA` |
| `PATCH`  | `/api/local/perfis/{nomePerfil}`                         | `GESTOR_LOJA` |

### 6.4 Fornecedores e encomendas

| Método | Endpoint | Role |
|---|---|---|
| `GET`    | `/api/local/fornecedores`                                  | autenticado |
| `POST`   | `/api/local/fornecedores`                                  | `GESTOR_LOJA` |
| `PATCH`  | `/api/local/fornecedores/{idFornecedor}`                   | `GESTOR_LOJA` |
| `PATCH`  | `/api/local/fornecedores/{idFornecedor}/inativar`          | `GESTOR_LOJA` |
| `POST`   | `/api/local/fornecedores/associar`                         | `GESTOR_LOJA` |
| `GET`    | `/api/local/fornecedores/{idFornecedor}/produtos`          | autenticado |
| `GET`    | `/api/local/encomendas`                                    | autenticado |
| `POST`   | `/api/local/encomendas`                                    | `GESTOR_LOJA` |
| `PATCH`  | `/api/local/encomendas/{idEncomenda}/estado`               | `GESTOR_LOJA` |

### 6.5 Inventário

| Método | Endpoint | Role |
|---|---|---|
| `GET`  | `/api/local/inventario`            | autenticado |
| `POST` | `/api/local/inventario/movimentos` | `GESTOR_LOJA` |

### 6.6 Vendas e devoluções (POS)

| Método | Endpoint | Role |
|---|---|---|
| `POST` | `/api/local/vendas/iniciar`                  | autenticado |
| `POST` | `/api/local/vendas/{idVenda}/linhas`         | autenticado |
| `POST` | `/api/local/vendas/{idVenda}/registar`       | autenticado |
| `GET`  | `/api/local/vendas`                          | autenticado |
| `POST` | `/api/local/vendas/{idVenda}/devolucoes`     | autenticado |
| `GET`  | `/api/local/devolucoes`                      | autenticado |
| `GET`  | `/api/local/devolucoes/linhas`               | autenticado |

### 6.7 Promoções

| Método | Endpoint | Role |
|---|---|---|
| `GET`  | `/api/local/promocoes/ativas`                | autenticado |
| `POST` | `/api/local/promocoes`                       | `GESTOR_LOJA` |
| `POST` | `/api/local/promocoes/{idPromocao}/cancelar` | `GESTOR_LOJA` |

### 6.8 Sincronização

| Método | Endpoint | Role |
|---|---|---|
| `POST` | `/api/local/sincronizacao/executar`        | `GESTOR_LOJA` |
| `GET`  | `/api/local/sincronizacao/disponibilidade` | autenticado |

### 6.9 Estatísticas (dashboard local)

| Método | Endpoint |
|---|---|
| `GET`  | `/api/local/estatisticas/kpi` |
| `GET`  | `/api/local/estatisticas/vendas-mensais` |
| `GET`  | `/api/local/estatisticas/vendas-hora` |
| `GET`  | `/api/local/estatisticas/vendas-categoria` |

### 6.10 Misc

| Método | Endpoint |
|---|---|
| `GET`  | `/api/local/node/role` — devolve `LOCAL` ou `CENTRAL` |

---

## 7. API Global — Endpoints

Base URL: `$BASE_GLOBAL = http://localhost:8081`. Disponível **apenas** em modo CENTRAL.
Scopes: `leituraGlobal` (qualquer gestor central) e `gestaoGlobal` (`GESTOR_CENTRAL`).

### 7.1 Autenticação

| Método | Endpoint |
|---|---|
| `POST` | `/api/global/auth/login` |
| `POST` | `/api/global/auth/logout` |
| `GET`  | `/api/global/auth/me` |
| `POST` | `/api/global/auth/verify-password` |

### 7.2 Lojas

| Método | Endpoint | Scope |
|---|---|---|
| `GET`    | `/api/global/lojas`             | `leituraGlobal` |
| `GET`    | `/api/global/lojas/{idLoja}`    | `leituraGlobal` |
| `POST`   | `/api/global/lojas`             | `gestaoGlobal` |
| `PATCH`  | `/api/global/lojas/{idLoja}`    | `gestaoGlobal` |
| `DELETE` | `/api/global/lojas/{idLoja}`    | `gestaoGlobal` |

### 7.3 Catálogo Central

| Método | Endpoint | Scope |
|---|---|---|
| `GET`   | `/api/global/catalogo/categorias`                       | `leituraGlobal` |
| `POST`  | `/api/global/catalogo/categorias`                       | `gestaoGlobal` |
| `POST`  | `/api/global/catalogo/produtos`                         | `gestaoGlobal` |
| `GET`   | `/api/global/catalogo/produtos/{codigoBarras}`          | `leituraGlobal` |
| `PATCH` | `/api/global/catalogo/produtos/{idProduto}/inativar`    | `gestaoGlobal` |

### 7.4 Funcionários e perfis (administração global)

| Método | Endpoint | Scope |
|---|---|---|
| `GET`    | `/api/global/admin/funcionarios`                              | `gestaoGlobal` |
| `POST`   | `/api/global/admin/funcionarios`                              | `gestaoGlobal` |
| `POST`   | `/api/global/admin/funcionarios/{idFuncionario}/bloquear`     | `gestaoGlobal` |
| `GET`    | `/api/global/funcionarios`                                    | `leituraGlobal` |
| `POST`   | `/api/global/funcionarios`                                    | `gestaoGlobal` |
| `PATCH`  | `/api/global/funcionarios/{idFuncionario}`                    | `gestaoGlobal` |
| `POST`   | `/api/global/funcionarios/{idFuncionario}/bloquear`           | `gestaoGlobal` |
| `POST`   | `/api/global/funcionarios/{idFuncionario}/desbloquear`        | `gestaoGlobal` |
| `DELETE` | `/api/global/funcionarios/{idFuncionario}`                    | `gestaoGlobal` |
| `PATCH`  | `/api/global/funcionarios/{idFuncionario}/perfil`             | `gestaoGlobal` |
| `GET`    | `/api/global/perfis`                                          | `leituraGlobal` |
| `POST`   | `/api/global/perfis`                                          | `gestaoGlobal` |
| `PATCH`  | `/api/global/perfis/{nomePerfil}`                             | `gestaoGlobal` |

### 7.5 Fornecedores e operações

| Método | Endpoint | Scope |
|---|---|---|
| `GET`    | `/api/global/fornecedores`                              | `leituraGlobal` |
| `POST`   | `/api/global/fornecedores`                              | `gestaoGlobal` |
| `PATCH`  | `/api/global/fornecedores/{idFornecedor}/inativar`      | `gestaoGlobal` |
| `GET`    | `/api/global/produtos`                                  | `leituraGlobal` |
| `GET`    | `/api/global/vendas`                                    | `leituraGlobal` |
| `GET`    | `/api/global/devolucoes`                                | `leituraGlobal` |
| `GET`    | `/api/global/encomendas`                                | `leituraGlobal` |
| `GET`    | `/api/global/promocoes/ativas`                          | `leituraGlobal` |
| `POST`   | `/api/global/promocoes`                                 | `gestaoGlobal` |
| `POST`   | `/api/global/promocoes/{idPromocao}/cancelar`           | `gestaoGlobal` |

### 7.6 Relatórios e exportações

| Método | Endpoint | Scope |
|---|---|---|
| `GET` | `/api/global/faturas/saft`                  | `leituraGlobal` |
| `GET` | `/api/global/faturas/{idFatura}/pdf`        | `leituraGlobal` |
| `GET` | `/api/global/relatorios/vendas`             | `leituraGlobal` |
| `GET` | `/api/global/relatorios/vendas/pdf`         | `leituraGlobal` |
| `GET` | `/api/global/relatorios/vendas-mensais`     | `leituraGlobal` |
| `GET` | `/api/global/relatorios/inventario`         | `leituraGlobal` |
| `GET` | `/api/global/relatorios/inventario/pdf`     | `leituraGlobal` |

### 7.7 Estatísticas (dashboard central)

| Método | Endpoint |
|---|---|
| `GET` | `/api/global/estatisticas/volume-vendas` |
| `GET` | `/api/global/estatisticas/ticket-medio` |
| `GET` | `/api/global/estatisticas/dashboard-kpis` |
| `GET` | `/api/global/estatisticas/kpi-gerais` |
| `GET` | `/api/global/estatisticas/vendas-mensais` |
| `GET` | `/api/global/estatisticas/vendas-horarias` |

---

## 8. Cenários E2E de Integração

Cada cenário assume infraestrutura limpa (§2.3) e usa `curl` com cookies, mas **deve também ser executado pela UI** (§10) para validar fluxos visuais.

### 8.1 Operador faz uma venda completa

1. Login `operador1@taki.pt`.
2. `POST /api/local/vendas/iniciar` → guardar `idVenda` da resposta.
3. `POST /api/local/vendas/{idVenda}/linhas` (× N) com `{ idProduto, quantidade }` válidos.
4. `POST /api/local/vendas/{idVenda}/registar` com método de pagamento.
5. Confirmar em `GET /api/local/vendas` que a venda aparece com estado terminal e total ≠ 0.

**Resultado esperado:** venda persistida, *outbox* com evento de sincronização pronto, stock decrementado em `GET /api/local/inventario`.

### 8.2 Devolução parcial

1. A partir da venda do §8.1, `POST /api/local/vendas/{idVenda}/devolucoes` com uma linha.
2. `GET /api/local/devolucoes` e `GET /api/local/devolucoes/linhas` confirmam a devolução.
3. `GET /api/local/inventario` mostra o stock restituído da quantidade devolvida.

### 8.3 Gerente cria produto e o operador vende-o

1. Login `tiago.rocha@taki.pt` → `POST /api/local/produtos` com produto novo (anotar `idProduto`).
2. Login `operador1@taki.pt` → fluxo de venda do §8.1 usando o novo produto.

### 8.4 Gestão de stock

1. Login gerente da loja N.
2. `POST /api/local/inventario/movimentos` com `tipo=ENTRADA` ou `SAIDA` e quantidade.
3. `GET /api/local/inventario` deve refletir o saldo atualizado.

### 8.5 Bloqueio e desbloqueio de funcionário

1. Login gerente → `POST /api/local/funcionarios/{idFuncionario}/bloquear`.
2. Tentar login com esse funcionário → **401**.
3. `POST /api/local/funcionarios/{idFuncionario}/desbloquear` → login passa a funcionar.

### 8.6 Encomenda a fornecedor — máquina de estados

1. `POST /api/local/encomendas` (estado inicial `PENDENTE`).
2. `PATCH /api/local/encomendas/{idEncomenda}/estado` para `ENVIADA` → `RECEBIDA`.
3. Transições inválidas (ex.: `PENDENTE → RECEBIDA` se a máquina não o permitir) devolvem **400/409**.
4. Ao chegar a `RECEBIDA`, o stock dos produtos da encomenda incrementa.

### 8.7 Promoção ativa

1. Gerente → `POST /api/local/promocoes` (ex.: % de desconto, janela temporal).
2. `GET /api/local/promocoes/ativas` lista a promoção.
3. Venda de um produto coberto reflete o desconto no total.
4. `POST /api/local/promocoes/{idPromocao}/cancelar` remove a promoção da lista de ativas.

### 8.8 Gestão central de catálogo

1. Login `dono@taki.pt` na API Global.
2. `POST /api/global/catalogo/produtos` regista um produto novo no catálogo central.
3. Após sincronização (§9), o produto deve aparecer em `GET /api/local/produtos` em todas as lojas.

---

## 9. Sincronização Loja ↔ Sede

Pré-condições: sede a correr em `192.168.1.82:8081` e `URLS_SEDE_CSV` configurado no `.env` da loja.

### 9.1 Disponibilidade

```bash
curl -b $COOKIES -s $BASE_LOCAL/api/local/sincronizacao/disponibilidade | jq .
```

✅ Reporta se a sede está alcançável e se há eventos pendentes no *outbox* local.

### 9.2 Executar sincronização (manual)

```bash
curl -b $COOKIES -X POST $BASE_LOCAL/api/local/sincronizacao/executar | jq .
```

✅ Eventos do *outbox* da loja são consumidos pela sede; após o ciclo, `GET /api/global/vendas` (na sede) deve incluir as vendas recentes da loja.

### 9.3 Resiliência

1. Parar o container da sede (`docker stop li4_backend_central`).
2. Realizar vendas na loja — devem completar localmente.
3. Voltar a iniciar a sede e executar `POST /api/local/sincronizacao/executar`.
4. Os eventos acumulados são entregues sem perdas e sem duplicação (idempotência por UUID).

---

## 10. Testes de Interface (UI)

Acesso ao frontend: `http://<IP_DO_SERVIDOR>:5173`. Como o Vite arranca com `--host 0.0.0.0`, qualquer dispositivo na mesma LAN acede.

### 10.1 Login e routing por papel

- Login com `dono@taki.pt` em `http://<IP_SEDE>:5173` → dashboard central com KPIs agregados.
- Login com `tiago.rocha@taki.pt` em `http://<IP_LOJA1>:5173` → dashboard da loja 1.
- Login com `operador1@taki.pt` → vai diretamente para o POS (sem acesso a gestão).

### 10.2 POS (Operador)

1. Criar venda, adicionar produtos via campo de pesquisa **e** via leitor de código de barras (atua como teclado).
2. Aplicar quantidades, validar subtotal com IVA.
3. Concluir a venda; confirmar geração da fatura/recibo PDF no browser.
4. Realizar devolução parcial; confirmar atualização do stock visível na UI do gerente.

### 10.3 Gestão (Gerente)

- CRUD de produtos, categorias, fornecedores e funcionários da própria loja.
- Atribuição de perfis a funcionários.
- Criação de encomenda e transições de estado.
- Visualização das estatísticas locais (KPIs, vendas mensais, vendas por hora, vendas por categoria).

### 10.4 Sede (Gestor Central)

- Visualizar todas as lojas em `/lojas`.
- Catálogo central: adicionar produtos e categorias visíveis em todas as filiais após sync.
- Exportar relatórios (PDF / SAF-T).
- Dashboard com KPIs agregados de toda a rede.

---

## 11. Auditoria de Segurança

| Teste | Comando / passo | Esperado |
|---|---|---|
| Endpoint protegido sem cookie | `curl -s -o /dev/null -w "%{http_code}" $BASE_LOCAL/api/local/produtos` | `401` |
| Operador tenta criar produto | login operador → `POST /api/local/produtos` | `403` |
| Gerente loja 1 acede a dados loja 2 (API Local) | login `tiago.rocha@taki.pt` em servidor da loja 2 | `401`/`403` (não é funcionário daquela loja) |
| Gerente de loja tenta endpoint global | login gerente → `GET $BASE_GLOBAL/api/global/lojas` | `401`/`403` |
| Cookie HttpOnly | inspecionar `Set-Cookie` no login | flag `HttpOnly` presente |
| Tampering JWT | editar cookie e refazer pedido | `401` |
| CORS de origem não autorizada | `curl -H "Origin: http://evil.example" -i $BASE_LOCAL/api/local/produtos` | sem header `Access-Control-Allow-Origin` para origens fora de `TAKI_CORS_ORIGIN` |
| Logout invalida sessão | logout → reutilizar cookie | `401` |
| Bloqueio de conta | bloquear funcionário e tentar login | `401`/`403` com mensagem de conta bloqueada |

---

## 12. Tabela de Erros Esperados

| Cenário | HTTP | Notas |
|---|---|---|
| Credenciais inválidas no login | `401` | Sem cookie emitido |
| Conta bloqueada | `401`/`403` | Mensagem específica |
| Cookie em falta ou expirado | `401` | Refazer login |
| Papel sem permissão | `403` | Ex.: operador a tentar gerir |
| Recurso inexistente (`UUID`) | `404` | |
| Corpo JSON malformado | `400` | |
| Transição de estado inválida (encomenda) | `400`/`409` | Conforme máquina de estados |
| Stock insuficiente em venda | `409` | A venda é rejeitada |
| Conflito de sincronização | `409` | Ver logs do *outbox* |
| Erro interno | `500` | Verificar logs do container `li4_backend*` |

---

## Anexo — Comandos úteis

```bash
# Logs do backend (sede)
docker logs -f li4_backend_central

# Aceder à BD da sede
docker exec -it li4_db_central psql -U admin -d taki_central_db

# Aceder à BD da loja 1
docker exec -it li4_db_local_1 psql -U admin -d taki_local_db_1

# Resetar tudo
docker compose -f docker-compose-central.yml down -v --remove-orphans
docker compose -f docker-compose.yml         down -v --remove-orphans
```
