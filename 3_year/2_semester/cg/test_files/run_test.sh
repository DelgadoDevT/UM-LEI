#!/bin/bash

# Script para correr testes de fase específica
# Uso: ./run_test.sh <fase> <numero_teste>
# Exemplo: ./run_test.sh 1 1

# Validação de argumentos
if [ $# -lt 2 ]; then
  echo "Uso: $0 <fase> <numero_teste>"
  echo "Exemplo: $0 1 1"
  exit 1
fi

FASE=$1
NUMERO=$2
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="${SCRIPT_DIR}/.."
TEST_DIR="${ROOT_DIR}/test_files/test_files_phase_${FASE}"
TEST_FILE="${TEST_DIR}/test_${FASE}_${NUMERO}.xml"
GENERATOR="${ROOT_DIR}/build/generator"
ENGINE="${ROOT_DIR}/build/engine"

# Verificar se o arquivo de teste existe
if [ ! -f "$TEST_FILE" ]; then
  echo "Erro: Arquivo de teste não encontrado: $TEST_FILE"
  exit 1
fi

# Criar diretório temporário para os arquivos .3d gerados
TEMP_DIR=$(mktemp -d)
echo "Diretório temporário: $TEMP_DIR"

# Copiar ficheiros .patch para o diretório temporário
cp "$ROOT_DIR"/*.patch "$TEMP_DIR" 2>/dev/null || true
cp "$TEST_DIR"/* "$TEMP_DIR" 2>/dev/null || true
cp "${SCRIPT_DIR}/test_files_phase_3"/*.patch "$TEMP_DIR" 2>/dev/null || true

# Extrair e executar os comandos do generator dos comentários XML
# Os comentários têm o formato: <!-- generator ... -->
grep '<!-- generator' "$TEST_FILE" | sed 's/.*<!-- generator \(.*\) -->.*/\1/' | while read -r cmd; do
  echo "Executando: generator $cmd"
  cd "$TEMP_DIR"
  "$GENERATOR" $cmd
  cd "$ROOT_DIR" >/dev/null
done

# Criar cópia do arquivo XML no diretório temporário com caminhos ajustados
cp "$TEST_FILE" "$TEMP_DIR/test.xml"

# Ajustar caminhos dos arquivos .3d no XML (remover prefixos de caminho se houver)
sed -i 's|file="[^"]*\/\([^"]*\.3d\)"|file="\1"|g' "$TEMP_DIR/test.xml"

# Executar o engine (no diretório temporário para encontrar os arquivos .3d)
echo ""
echo "Executando engine com arquivo: $TEMP_DIR/test.xml"
cd "$TEMP_DIR"
FREEGLUT_WAYLAND_DECORATIONS=1 "$ENGINE" "test.xml"
cd "$ROOT_DIR" >/dev/null

# Limpeza (comentado para permitir inspeção dos arquivos gerados)
# rm -rf "$TEMP_DIR"
echo ""
echo "Arquivos gerados em: $TEMP_DIR"
