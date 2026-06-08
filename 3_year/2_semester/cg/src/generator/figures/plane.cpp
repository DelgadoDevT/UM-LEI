/**
 * @file plane.cpp
 * @brief Implementation of the Plane class methods.
 */
#include "plane.h"
#include <cmath>

using namespace std;

/**
 * @brief Constructor for the Plane class.
 * @param l The length of the plane.
 * @param d The number of divisions for the grid.
 */
Plane::Plane(float l, int d) {
    this->length = l;
    this->divisions = d;
}

/**
 * @brief Generates the vertices, normals, and texture coordinates for the plane.
 * @return A vector of Vertex objects representing the generated plane.
 */
vector<Vertex> Plane::generate() {
    vector<Vertex> vertices;
   
    // Calculate where the plane starts
    float half = length / 2.0f;
    float step = length / (float)divisions; 

    for (int i = 0; i < divisions; ++i) {
        for (int j = 0; j < divisions; ++j) {
            
            // Calculate the X and Z coordinates for the 4 corners of the current grid cell
            float x1 = -half + (i * step);
            float x2 = -half + ((i + 1) * step);
            
            float z1 = -half + (j * step);
            float z2 = -half + ((j + 1) * step);

            // Normal points straight up (Y+)
            float nx = 0.0f, ny = 1.0f, nz = 0.0f;

            // Texture coordinates (normalized)
            float tu1 = (float)i / (float)divisions;
            float tu2 = (float)(i + 1) / (float)divisions;
            float tv1 = 1.0f - ((float)j / (float)divisions);
            float tv2 = 1.0f - ((float)(j + 1) / (float)divisions);

            // Triangle 1 
            vertices.push_back({x1, 0.0f, z1, nx, ny, nz, tu1, tv1});
            vertices.push_back({x1, 0.0f, z2, nx, ny, nz, tu1, tv2});
            vertices.push_back({x2, 0.0f, z2, nx, ny, nz, tu2, tv2});

            // Triangle 2 
            vertices.push_back({x2, 0.0f, z2, nx, ny, nz, tu2, tv2});
            vertices.push_back({x2, 0.0f, z1, nx, ny, nz, tu2, tv1});
            vertices.push_back({x1, 0.0f, z1, nx, ny, nz, tu1, tv1});
        }
    }

    return vertices;
}
