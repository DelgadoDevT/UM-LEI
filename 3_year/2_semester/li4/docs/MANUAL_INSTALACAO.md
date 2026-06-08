# Manual de Instalação e Desdobramento em Produção - Sistema Taki

Este documento detalha os procedimentos rigorosos para a instalação e configuração do sistema **Taki** num ambiente de produção real. Diferente do ambiente de desenvolvimento (que utiliza um único `docker-compose.yml` para fins de demonstração), o ambiente de produção reflete a verdadeira arquitetura física distribuída da empresa: **4 lojas físicas**, sendo que a loja sede (Famalicão) aloja também a infraestrutura central de dados e gestão.

---

## 1. Arquitetura de Rede e Topologia (Hub-and-Spoke)

A arquitetura reflete diretamente a orgânica da empresa delineada no caso de estudo:

*   **Sede e Nó Central (Famalicão - Loja ID 1):**
    *   **Infraestrutura Central:** Um contentor Docker isolado (ou máquina dedicada) contendo apenas a Base de Dados Central (`taki_central_db`). Esta recebe toda a informação consolidada da rede.
    *   **Infraestrutura Local (Famalicão):** Na mesma loja física, existe um servidor para a operação da filial, composto por um contentor para a Base de Dados Local (`taki_local_db_1`) e o Backend da aplicação a correr no modo `central`. Expõe simultaneamente a API Local (para as caixas de Famalicão) e a API Global (para gestão e comunicação inter-lojas).
*   **Lojas Satélite (Felgueiras ID 2, Madeira ID 3, Cabeceiras ID 4):**
    *   **Servidor Local (1 por loja):** Um servidor físico em cada localidade. Aloja o seu próprio contentor da Base de Dados Local (`taki_local_db_X`) e o Backend da aplicação configurado estritamente no modo `local`.
*   **Terminais de Caixa (POS) e Computadores de Gestão:**
    *   Dispositivos _cliente_ sem instalação local, distribuídos pelas diversas zonas da loja, que acedem à aplicação web através do servidor da sua rede local (LAN).

---

## 2. Pré-requisitos do Sistema

### 2.1. Servidores (Sede e Locais)
**Nota Importante:** Estes servidores são máquinas dedicadas (headless), instaladas no bastidor ou _back-office_ de cada loja. **Nenhum funcionário trabalha diretamente no servidor.**
*   **Sistema Operativo:** Ubuntu Server 22.04 LTS (ou superior, sem interface gráfica).
*   **Hardware Mínimo:** 4 vCPUs, 8GB RAM, 100GB SSD NVMe (garantia de IOPS rápidas para a base de dados PostgreSQL e HikariCP).
*   **Software Obrigatório Instalado:**
    *   **Docker Engine** e **Docker Compose** (v2).
    *   **Nginx** (para atuar como _reverse proxy_ e efetuar a terminação de certificados SSL/TLS).

### 2.2. Terminais (Caixas Registadoras / POS)
*   Equipamento tátil genérico com rato/teclado.
*   Leitor de código de barras USB (_Keyboard Wedge_ - emula escrita no teclado).
*   Navegador Moderno atualizado (Google Chrome, Firefox ou Microsoft Edge).
*   Ligação por cabo Ethernet à LAN da loja.

---

## 3. Passo 1: Preparação Criptográfica (Obrigatório)

O sistema de autenticação recorre a Web Sessions seguras através de JWTs (*JSON Web Tokens*) assinados assimetricamente (`RS256`).

Para que os perfis de utilizador da Central (ex: Dono da rede) e as permissões fluam de forma segura e sejam lidos e validados por qualquer loja, **as chaves criptográficas têm de ser geradas uma vez e instaladas de forma idêntica em todos os servidores (Sede e Filiais).**

Num ambiente seguro, execute os seguintes comandos:
```bash
# 1. Gerar a chave privada
openssl genpkey -algorithm RSA -out taki_private.pem -pkeyopt rsa_keygen_bits:2048

# 2. Extrair a chave pública
openssl rsa -pubout -in taki_private.pem -out taki_public.pem
```

Transfira estas chaves de forma segura (ex: via `scp` ou SFTP) para todos os servidores de destino.

---

## 4. Passo 2: Instalação na Sede (Famalicão - Hub Central)

Na sede, é recomendado separar o Docker Central do Docker da Loja.

### 4.1. Arranque da Base de Dados Central
No diretório `/opt/taki/central`, crie o `docker-compose.central.yml`:
```yaml
services:
  db_central:
    image: postgres:16-alpine
    container_name: taki_db_central
    restart: always
    environment:
      POSTGRES_DB: taki_central_db
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: ${DB_CENTRAL_PASS}
    ports:
      - "5432:5432"
    volumes:
      - pgdata_central:/var/lib/postgresql/data
      - ./database/schema_postgres.sql:/docker-entrypoint-initdb.d/1-schema.sql
      - ./database/logica_ativa_postgres.sql:/docker-entrypoint-initdb.d/2-logica_ativa.sql
      - ./database/sincronizacao_postgres.sql:/docker-entrypoint-initdb.d/3-sincronizacao.sql
      - ./database/user_roles.sql:/docker-entrypoint-initdb.d/4-roles.sql
      - ./database/indices_postgres.sql:/docker-entrypoint-initdb.d/5-indices.sql
volumes:
  pgdata_central:
```
Efetue o povoamento do catálogo e administradores centrais executando:
```bash
./database/load_data.sh central
```

### 4.2. Arranque da Operação Local e Global (Loja 1)
Num diretório distinto (`/opt/taki/loja1`), crie um ficheiro `.env` com a configuração sensível do backend e as credenciais geradas:
```env
APP_MODE=central
TAKI_DB_URL=jdbc:postgresql://db_local_1:5432/taki_local_db_1
TAKI_DB_USER=admin
TAKI_DB_PASSWORD=senha_forte_aqui
TAKI_JWT_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n..."
TAKI_JWT_PUBLIC_KEY="-----BEGIN PUBLIC KEY-----\n..."
```

O respetivo `docker-compose.yml` para gerir a loja sede e o motor central da aplicação:
```yaml
services:
  db_local_1:
    image: postgres:16-alpine
    container_name: taki_db_local_1
    restart: always
    environment:
      POSTGRES_DB: taki_local_db_1
      # (restantes variáveis do Postgres omitidas)
    volumes:
      - pgdata_local_1:/var/lib/postgresql/data
      # (Mapeamento dos scripts SQL idênticos à base central)

  backend:
    image: pt.uminho.taki/backend:latest
    container_name: taki_backend_central
    env_file: .env
    ports:
      - "8080:8080" # API Local
      - "8081:8081" # API Global
    depends_on:
      - db_local_1

  frontend:
    image: pt.uminho.taki/frontend:latest
    container_name: taki_frontend
    env_file: .env
    ports:
      - "80:80"
```
Execute o povoamento da loja:
```bash
./database/load_data.sh 1
```

---

## 5. Passo 3: Instalação das Lojas Satélite (Filiais 2, 3 e 4)

Nos servidores físicos alojados em Felgueiras, Madeira e Cabeceiras de Basto, a infraestrutura é consideravelmente mais simples e isolada.

1. **Copiar o Repositório:** Alojado em `/opt/taki/local`.
2. **Configuração do `.env`:** Prestar especial atenção a estes valores-chave:
```env
APP_MODE=local
TAKI_DB_URL=jdbc:postgresql://db_local:5432/taki_local_db_X  # Onde X é 2, 3 ou 4
# Inserir o par de chaves JWT idêntico ao da Sede!
```
3. **Serviços (Docker):** O servidor na filial apenas necessita da sua Base de Dados Local, um Backend (em `APP_MODE=local` expõe apenas a porta 8080) e o Frontend.
4. **Povoamento:** Execute o script com o ID respetivo para inicializar a filial:
```bash
./database/load_data.sh 2 # (Use 3 para Madeira, 4 para Cabeceiras)
```

---

## 6. Passo 4: Operacionalização das Caixas Registadoras e Terminais de Gestão

É vital compreender que **o sistema funciona exclusivamente via Web**. As Caixas Registadoras (POS) e os computadores de secretária dos gestores são clientes "burros" (_Thin Clients_). **Não há instalação de Docker, bases de dados ou de código nestes equipamentos.** Eles comunicam através da rede interna (LAN) com o Servidor Local preparado no Passo 3.

### Ligação Física e de Rede
1. Ligue os computadores/POS à mesma rede local (Cabo Ethernet ou Wi-Fi interno) onde o servidor se encontra ligado.
2. Certifique-se de que cada equipamento alcança o IP do servidor local (ex: num terminal do Gestor, execute `ping 192.168.1.100` — sendo este o IP do servidor _back-office_).

### Acesso ao Software (Frontend)
1. Nos POS, abra o navegador web (Chrome/Edge) em "Modo Quiosque" (Full-Screen) para bloquear o acesso do operador ao sistema operativo subjacente.
2. Na barra de endereços (ou atalho no ambiente de trabalho), introduza o IP ou domínio interno do servidor local daquela loja (ex: `https://servidor.loja.taki/` ou `http://192.168.1.100`). **Toda a interface Taki será descarregada pelo browser.**
3. O operador/gestor efetua o login. A comunicação flui pela rede local até à API do servidor que validará as credenciais.
4. **Periféricos (Impressoras e Leitores):** 
    - O leitor de código de barras USB liga-se ao POS e atua apenas como teclado automático. 
    - Para faturas, o servidor Taki gera o PDF. Quando o ficheiro chega ao browser do POS, o sistema de impressão nativo do sistema operativo da caixa trata de enviá-lo para a impressora térmica/A4.

---

## 7. Notas Operacionais Finais

*   **Autonomia em Caso de Falha:** Graças à topologia e ao padrão arquitetural _Outbox_ nos DAOs, se a ligação VPN (ou fibra) entre uma Filial e a Sede (Famalicão) for abaixo, **a loja continuará a operar, vender e registar faturas a 100% de normalidade**. As informações de sincronização irão acumular na sua base de dados local até que o estado de rede seja restabelecido, altura em que o nó central processará os eventos.
*   **Segurança:** A Firewall da sede deve autorizar tráfego na porta 8081 **exclusivamente** proveniente dos endereços IP (VPN/Estáticos) das lojas filiais, para assegurar que a rotina de sincronização entre as Filiais e o nó `central` permaneça numa rede privada virtual.