#!/bin/bash

# Script para correr o Demo do Sistema Solar (Fase 4)
# Localização: demo_sistema_solar/run_demo.sh

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
XML_FILE="${SCRIPT_DIR}/sistemasolar.xml"
GENERATOR="${ROOT_DIR}/build/generator"
ENGINE="${ROOT_DIR}/build/engine"

# Cores para o output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Demo Sistema Solar (Fase 4) ===${NC}"

# 1. Verificar binários
if [ ! -f "$GENERATOR" ] || [ ! -f "$ENGINE" ]; then
    echo -e "${RED}Erro: Binários não encontrados em ${ROOT_DIR}/build/${NC}"
    echo "Por favor, compila o projeto primeiro:"
    echo "  mkdir -p build && cd build && cmake .. && make"
    exit 1
fi

# 2. Criar diretório para ficheiros .3d e execução
DEMO_BUILD_DIR="${SCRIPT_DIR}/build_demo"
mkdir -p "$DEMO_BUILD_DIR"
cd "$DEMO_BUILD_DIR" || exit

echo -e "${GREEN}A gerar modelos .3d...${NC}"

# Gerar Esferas (Sol, Planetas e Luas)
echo "  > sphere_1_16_16.3d"
"$GENERATOR" sphere 1 50 50 sphere_1_16_16.3d > /dev/null
echo "  > sphere_1_8_8.3d"
"$GENERATOR" sphere 1 32 32 sphere_1_8_8.3d > /dev/null

# Gerar Torus (Anéis de Saturno)
# Parâmetros: innerRadius=0.05, outerRadius=1.5, slices=30, stacks=30
echo "  > torus_005_15_30_30.3d (Anéis de Saturno)"
"$GENERATOR" torus 0.25 1.5 60 60 torus.3d > /dev/null

# Gerar Asteroide (Cometa) utilizando a nova primitiva do generator
# Parâmetros: radius=1, slices=20, stacks=20, seed=42, roughness=0.1
echo "  > cometa.3d (nova primitiva cometa)"
"$GENERATOR" cometa 1 20 20 42 0.1 cometa.3d > /dev/null

# 3. Preparar XML
cp "$XML_FILE" .

# 4. Executar Engine
echo -e "${GREEN}A iniciar o motor gráfico...${NC}"
FREEGLUT_WAYLAND_DECORATIONS=1 "$ENGINE" sistemasolar.xml

echo -e "${BLUE}Demo terminada.${NC}"
cd "$ROOT_DIR" || exit
