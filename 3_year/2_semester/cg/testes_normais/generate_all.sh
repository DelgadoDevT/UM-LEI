#!/bin/bash

# Este script automatiza a geração das primitivas 3D para a Fase 4
# Executa-o no terminal com: ./generate_all.sh
# (Não te esqueças de dar permissão de execução antes: chmod +x generate_all.sh)

# Caminho para o teu executável do generator (ajusta se necessário, ex: build/generator)
GENERATOR="./../build/generator"

echo "A iniciar a geração de modelos..."

echo "1. A gerar Plano..."
# Assumindo: generator plane <tamanho> <divisoes> <output>
$GENERATOR plane 2 5 plane.3d

echo "2. A gerar Caixa..."
# Assumindo: generator box <tamanho> <divisoes> <output>
$GENERATOR box 2 5 box.3d

echo "3. A gerar Esfera..."
# Assumindo: generator sphere <raio> <fatias> <camadas> <output>
$GENERATOR sphere 2 30 30 sphere.3d

echo "4. A gerar Cone..."
# Assumindo: generator cone <raio_base> <altura> <fatias> <camadas> <output>
$GENERATOR cone 1 2 15 15 cone.3d

echo "5. A gerar Torus..."
# Assumindo: generator torus <raio_interno> <raio_externo> <fatias> <camadas> <output>
$GENERATOR torus 0.5 1.5 30 30 torus.3d

echo "6. A gerar Superfície Bézier (Patch)..."
# Assumindo que tens um ficheiro teapot.patch ou cometa.patch na pasta
# Descomenta a linha abaixo e ajusta o nome do ficheiro de entrada se o tiveres disponível:
$GENERATOR patch ../test_files/test_files_phase_3/teapot.patch 10 patch.3d

echo "7. A gerar Cometa (Superfície Bézier)..."
# Assumindo: generator patch <ficheiro.patch> <tessellation> <output>
$GENERATOR cometa 1 20 20 42 0.1 cometa.3d

echo "Todos os modelos .3d foram gerados com sucesso!"