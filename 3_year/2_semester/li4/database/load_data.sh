#!/bin/bash
set -e

# =============================================================================
#  SCRIPT DE CARREGAMENTO DE DADOS PARA POSTGRESQL
# =============================================================================
#
# Modo de utilização:
# 1. Certifique-se que as variáveis de ambiente para a conexão estão definidas,
#    ou que o seu ficheiro .env na raiz do projeto está configurado.
#    (Ex: PGDATABASE, PGHOST, PGUSER, PGPASSWORD, PGPORT)
#
# 2. Corra o script indicando o ID da loja ou 'central':
#    ./load_data.sh 1            -> gera e carrega populate_loja_1.sql na DB local (taki_local_db)
#    ./load_data.sh central      -> gera e carrega populate_central.sql na DB central (taki_central_db)

# Diretoria onde o populate.py gerou os ficheiros (relativa ao script)
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
ROOT_DIR="$( cd "${SCRIPT_DIR}/.." &> /dev/null && pwd )"
SQL_BASE_DIR="${SCRIPT_DIR}/../population_output/sql"

# Carregar variáveis do ficheiro .env se existir
if [ -f "${ROOT_DIR}/.env" ]; then
    # Filtra comentários e exporta variáveis
    export $(grep -v '^#' "${ROOT_DIR}/.env" | xargs)
fi

# Valores por defeito se não estiverem no .env
export PGHOST=${PGHOST:-localhost}
export PGUSER=${PGUSER:-admin}
export PGPASSWORD=${PGPASSWORD:-password123}

# Verifica se o argumento foi passado
if [ -z "$1" ]; then
    echo "Erro: É necessário especificar o dataset a carregar (ID da loja ou 'central')."
    echo "Exemplos: 1, 2, 3, 4, central"
    exit 1
fi

DATA_SET=$1 # ID da loja ou 'central'

# Configura conexão baseada no dataset
if [ "$DATA_SET" == "central" ]; then
    export PGPORT=${PGPORT:-5432}
    export PGDATABASE=${PGDATABASE:-taki_central_db}
    SQL_FILE="${SQL_BASE_DIR}/populate_central.sql"
else # Assumimos que é um ID de loja local (1, 2, 3, 4)
    # Define o nome da DB e a porta com base no ID da loja
    case "$DATA_SET" in
        1)
            export PGPORT=${PGPORT:-5433}
            export PGDATABASE=${PGDATABASE:-taki_local_db_1}
            ;;
        2)
            export PGPORT=${PGPORT:-5434}
            export PGDATABASE=${PGDATABASE:-taki_local_db_2}
            ;;
        3)
            export PGPORT=${PGPORT:-5435}
            export PGDATABASE=${PGDATABASE:-taki_local_db_3}
            ;;
        4)
            export PGPORT=${PGPORT:-5436}
            export PGDATABASE=${PGDATABASE:-taki_local_db_4}
            ;;
        *)
            echo "Erro: ID de loja local inválido '${DATA_SET}'. Use 1, 2, 3, 4 ou 'central'."
            exit 1
            ;;
    esac
    SQL_FILE="${SQL_BASE_DIR}/populate_loja_${DATA_SET}.sql"
fi

# Gera o ficheiro SQL se não existir
if [ ! -f "$SQL_FILE" ]; then
    echo "Ficheiro SQL não encontrado: ${SQL_FILE}. A gerar dados com populate.py..."
    python3 "${SCRIPT_DIR}/populate.py" --loja_id "${DATA_SET}"
    if [ ! -f "$SQL_FILE" ]; then
        echo "Erro: populate.py não gerou o ficheiro esperado: ${SQL_FILE}"
        exit 1
    fi
fi

echo "A limpar dados da base '${PGDATABASE}'..."
psql -v ON_ERROR_STOP=1 --quiet -c "TRUNCATE TABLE linha_devolucao,devolucao,fatura,pagamento,linha_venda,venda,movimento_inventario,inventario,linha_encomenda,encomenda,promocao_produto,promocao_categoria,promocao,produto_categoria,produto_fornecedor,funcionario,produto,fornecedor,categoria,loja RESTART IDENTITY CASCADE;"

echo "A carregar dataset '${DATA_SET}' a partir de ${SQL_FILE}..."
psql -v ON_ERROR_STOP=1 --quiet -f "${SQL_FILE}"

LOJAS_CARREGADAS=$(psql -tA -v ON_ERROR_STOP=1 -c "SELECT count(*) FROM loja;")
if [[ "${LOJAS_CARREGADAS}" == "0" ]]; then
    echo "Erro: carregamento terminou sem dados (tabela loja vazia)."
    echo "Valide o ficheiro SQL gerado em ${SQL_FILE}."
    exit 1
fi

echo "Carregamento concluído com sucesso em '${PGDATABASE}' (dataset: ${DATA_SET})."
