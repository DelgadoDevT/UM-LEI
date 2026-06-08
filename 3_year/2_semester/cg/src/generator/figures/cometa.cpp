/**
 * @file cometa.cpp
 * @brief Implementation of the Cometa class methods.
 */
#include "cometa.h"
#include "generatorUtils.h"
#include <cmath>
#include <cstdlib>
#include <map>
#include <tuple>

using namespace std;

/**
 * @brief Constructor for the Cometa class.
 * @param r Base radius of the comet.
 * @param sl Number of slices.
 * @param st Number of stacks.
 * @param sd Random seed for surface displacement.
 * @param rough Surface roughness/displacement factor.
 */
Cometa::Cometa(float r, int sl, int st, int sd, float rough) {
    this->radius = r;
    this->slices = sl;
    this->stacks = st;
    this->seed = sd;
    this->roughness = rough;
}

/**
 * @typedef GridPos
 * @brief Structure to identify points (i, j) on the sphere grid.
 */
typedef tuple<int, int> GridPos;

/**
 * @brief Generates the vertices, normals, and texture coordinates for the comet.
 * @return A vector of Vertex objects representing the generated comet.
 */
vector<Vertex> Cometa::generate() {
    vector<Vertex> vertices;
    srand(seed);

    float alphaStep = (2.0f * M_PI) / (float)slices;
    float betaStep = M_PI / (float)stacks;

    // Mapa para guardar os raios deformados de cada ponto da grelha (i, j)
    // Para garantir que triângulos adjacentes partilham a mesma deformação
    map<GridPos, float> deformedRadii;

    /**
     * @brief Helper lambda to get or calculate the deformed radius at a specific grid position.
     * @param i The slice index.
     * @param j The stack index.
     * @return The deformed radius value.
     */
    auto getDeformedRadius = [&](int i, int j) {
        // Normalizar i (para que o fim da fatia encontre o início)
        int normI = i % slices;
        // J está limitado de 0 a stacks
        GridPos pos = make_tuple(normI, j);

        if (deformedRadii.find(pos) == deformedRadii.end()) {
            // Se j for o polo norte (0) ou sul (stacks), o raio deve ser o mesmo para todos os i
            if (j == 0 || j == stacks) {
                GridPos polePos = make_tuple(-1, j); // -1 para identificar o polo
                if (deformedRadii.find(polePos) == deformedRadii.end()) {
                    float offset = ((float)rand() / RAND_MAX * 2.0f - 1.0f) * roughness;
                    deformedRadii[polePos] = radius + offset;
                }
                return deformedRadii[polePos];
            }

            float offset = ((float)rand() / RAND_MAX * 2.0f - 1.0f) * roughness;
            deformedRadii[pos] = radius + offset;
        }
        return deformedRadii[pos];
    };

    for (int i = 0; i < slices; i++) {
        for (int j = 0; j < stacks; j++) {
            
            float a1 = i * alphaStep;
            float a2 = (i + 1) * alphaStep;

            float b1 = j * betaStep; 
            float b2 = (j + 1) * betaStep;

            float r1 = getDeformedRadius(i, j);
            float r2 = getDeformedRadius(i + 1, j);
            float r3 = getDeformedRadius(i, j + 1);
            float r4 = getDeformedRadius(i + 1, j + 1);

            // P1 (Top-Left)
            float x1 = r1 * sin(b1) * sin(a1);
            float y1 = r1 * cos(b1);
            float z1 = r1 * sin(b1) * cos(a1);

            // P2 (Top-Right)
            float x2 = r2 * sin(b1) * sin(a2);
            float y2 = r2 * cos(b1);
            float z2 = r2 * sin(b1) * cos(a2);

            // P3 (Bottom-Left)
            float x3 = r3 * sin(b2) * sin(a1);
            float y3 = r3 * cos(b2);
            float z3 = r3 * sin(b2) * cos(a1);

            // P4 (Bottom-Right)
            float x4 = r4 * sin(b2) * sin(a2);
            float y4 = r4 * cos(b2);
            float z4 = r4 * sin(b2) * cos(a2);

            float tu1 = (float)i / (float)slices;
            float tu2 = (float)(i + 1) / (float)slices;
            float tv1 = (float)j / (float)stacks;
            float tv2 = (float)(j + 1) / (float)stacks;

            float nx, ny, nz;

            if (j == 0) {
                // calculate face normal
                crossProduct(x3-x1, y3-y1, z3-z1, x4-x1, y4-y1, z4-z1, nx, ny, nz);
                normalize(nx, ny, nz);

                vertices.push_back({x1, y1, z1, nx, ny, nz, tu1, tv1});
                vertices.push_back({x3, y3, z3, nx, ny, nz, tu1, tv2});
                vertices.push_back({x4, y4, z4, nx, ny, nz, tu2, tv2});
            }
            else if (j == stacks - 1) {
                // calculate face normal
                crossProduct(x4-x1, y4-y1, z4-z1, x2-x1, y2-y1, z2-z1, nx, ny, nz);
                normalize(nx, ny, nz);

                vertices.push_back({x1, y1, z1, nx, ny, nz, tu1, tv1});
                vertices.push_back({x4, y4, z4, nx, ny, nz, tu2, tv2});
                vertices.push_back({x2, y2, z2, nx, ny, nz, tu2, tv1});
            }
            else {
                // Triangle 1 normal
                crossProduct(x3-x1, y3-y1, z3-z1, x4-x1, y4-y1, z4-z1, nx, ny, nz);
                normalize(nx, ny, nz);
                vertices.push_back({x1, y1, z1, nx, ny, nz, tu1, tv1});
                vertices.push_back({x3, y3, z3, nx, ny, nz, tu1, tv2});
                vertices.push_back({x4, y4, z4, nx, ny, nz, tu2, tv2});

                // Triangle 2 normal
                crossProduct(x4-x1, y4-y1, z4-z1, x2-x1, y2-y1, z2-z1, nx, ny, nz);
                normalize(nx, ny, nz);
                vertices.push_back({x1, y1, z1, nx, ny, nz, tu1, tv1});
                vertices.push_back({x4, y4, z4, nx, ny, nz, tu2, tv2});
                vertices.push_back({x2, y2, z2, nx, ny, nz, tu2, tv1});
            }
        }
    }

    return vertices;
}
