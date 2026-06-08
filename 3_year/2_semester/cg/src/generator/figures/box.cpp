/**
 * @file box.cpp
 * @brief Implementation of the Box class methods.
 */
#include "box.h"

using namespace std;

/**
 * @brief Constructor for the Box class.
 * @param u The dimension (units) of the box edges.
 * @param g The number of grid divisions for the box faces.
 */
Box::Box(float u, int g) {
    this->units = u;
    this->grid = g;
}

/**
 * @brief Generates the vertices, normals, and texture coordinates for the box.
 * @return A vector of Vertex objects representing the generated box.
 */
vector<Vertex> Box::generate() {
    vector<Vertex> vertices;

    // The cube spans from -half to +half on X, Y, and Z
    float half = units / 2.0f;
    float step = units / (float)grid; 

    // We loop through the grid rows and columns
    for (int i = 0; i < grid; ++i) {
        for (int j = 0; j < grid; ++j) {
            // Calculate the coordinates for the current grid cell
            float v1 = -half + i * step;
            float v2 = -half + j * step;
            float v1_next = -half + (i + 1) * step;
            float v2_next = -half + (j + 1) * step;

            // Texture coordinates (normalized for the current grid cell)
            float tu1 = (float)i / (float)grid;
            float tu2 = (float)(i + 1) / (float)grid;
            float tv1 = (float)j / (float)grid;
            float tv2 = (float)(j + 1) / (float)grid;

            // 1. Front face (Normal Z+)
            vertices.push_back({v1, v2, half, 0.0f, 0.0f, 1.0f, tu1, tv1});
            vertices.push_back({v1_next, v2, half, 0.0f, 0.0f, 1.0f, tu2, tv1});
            vertices.push_back({v1_next, v2_next, half, 0.0f, 0.0f, 1.0f, tu2, tv2});

            vertices.push_back({v1, v2, half, 0.0f, 0.0f, 1.0f, tu1, tv1});
            vertices.push_back({v1_next, v2_next, half, 0.0f, 0.0f, 1.0f, tu2, tv2});
            vertices.push_back({v1, v2_next, half, 0.0f, 0.0f, 1.0f, tu1, tv2});

            // 2. Back face (Normal Z-)
            vertices.push_back({v1, v2, -half, 0.0f, 0.0f, -1.0f, tu1, tv1});
            vertices.push_back({v1, v2_next, -half, 0.0f, 0.0f, -1.0f, tu1, tv2});
            vertices.push_back({v1_next, v2_next, -half, 0.0f, 0.0f, -1.0f, tu2, tv2});

            vertices.push_back({v1, v2, -half, 0.0f, 0.0f, -1.0f, tu1, tv1});
            vertices.push_back({v1_next, v2_next, -half, 0.0f, 0.0f, -1.0f, tu2, tv2});
            vertices.push_back({v1_next, v2, -half, 0.0f, 0.0f, -1.0f, tu2, tv1});

            // 3. Top face (Normal Y+)
            vertices.push_back({v1, half, v2, 0.0f, 1.0f, 0.0f, tu1, tv1});
            vertices.push_back({v1, half, v2_next, 0.0f, 1.0f, 0.0f, tu1, tv2});
            vertices.push_back({v1_next, half, v2_next, 0.0f, 1.0f, 0.0f, tu2, tv2});

            vertices.push_back({v1, half, v2, 0.0f, 1.0f, 0.0f, tu1, tv1});
            vertices.push_back({v1_next, half, v2_next, 0.0f, 1.0f, 0.0f, tu2, tv2});
            vertices.push_back({v1_next, half, v2, 0.0f, 1.0f, 0.0f, tu2, tv1});

            // 4. Bottom face (Normal Y-)
            vertices.push_back({v1, -half, v2, 0.0f, -1.0f, 0.0f, tu1, tv1});
            vertices.push_back({v1_next, -half, v2_next, 0.0f, -1.0f, 0.0f, tu2, tv2});
            vertices.push_back({v1, -half, v2_next, 0.0f, -1.0f, 0.0f, tu1, tv2});

            vertices.push_back({v1, -half, v2, 0.0f, -1.0f, 0.0f, tu1, tv1});
            vertices.push_back({v1_next, -half, v2, 0.0f, -1.0f, 0.0f, tu2, tv1});
            vertices.push_back({v1_next, -half, v2_next, 0.0f, -1.0f, 0.0f, tu2, tv2});

            // 5. Right face (Normal X+)
            vertices.push_back({half, v2, v1, 1.0f, 0.0f, 0.0f, tu2, tv1});
            vertices.push_back({half, v2_next, v1, 1.0f, 0.0f, 0.0f, tu2, tv2});
            vertices.push_back({half, v2_next, v1_next, 1.0f, 0.0f, 0.0f, tu1, tv2});

            vertices.push_back({half, v2, v1, 1.0f, 0.0f, 0.0f, tu2, tv1});
            vertices.push_back({half, v2_next, v1_next, 1.0f, 0.0f, 0.0f, tu1, tv2});
            vertices.push_back({half, v2, v1_next, 1.0f, 0.0f, 0.0f, tu1, tv1});

            // 6. Left face (Normal X-)
            vertices.push_back({-half, v2, v1, -1.0f, 0.0f, 0.0f, tu1, tv1});
            vertices.push_back({-half, v2, v1_next, -1.0f, 0.0f, 0.0f, tu2, tv1});
            vertices.push_back({-half, v2_next, v1_next, -1.0f, 0.0f, 0.0f, tu2, tv2});

            vertices.push_back({-half, v2, v1, -1.0f, 0.0f, 0.0f, tu1, tv1});
            vertices.push_back({-half, v2_next, v1_next, -1.0f, 0.0f, 0.0f, tu2, tv2});
            vertices.push_back({-half, v2_next, v1, -1.0f, 0.0f, 0.0f, tu1, tv2});
        }
    }

    return vertices;
}
