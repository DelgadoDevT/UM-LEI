/**
 * @file sphere.cpp
 * @brief Implementation of the Sphere class methods.
 */
#include "sphere.h"
#include <cmath>

using namespace std;

/**
 * @brief Constructor for the Sphere class.
 * @param r The radius of the sphere.
 * @param sl The number of slices.
 * @param st The number of stacks.
 */
Sphere::Sphere(float r, int sl, int st) {
    this->radius = r;
    this->slices = sl;
    this->stacks = st;
}

/**
 * @brief Generates the vertices, normals, and texture coordinates for the sphere.
 * @return A vector of Vertex objects representing the generated sphere.
 */
vector<Vertex> Sphere::generate() {
    vector<Vertex> vertices;

    float alphaStep = (2.0f * M_PI) / (float)slices;
    float betaStep = M_PI / (float)stacks;

    for (int i = 0; i < slices; i++) {
        for (int j = 0; j < stacks; j++) {
            
            float a1 = i * alphaStep;
            float a2 = (i + 1) * alphaStep;

            // Beta goes from 0 (North Pole) to PI (South Pole)
            float b1 = j * betaStep; 
            float b2 = (j + 1) * betaStep;

            // P1 & P2: Top ring vertices (at beta 1)
            // P3 & P4: Bottom ring vertices (at beta 2)
            
            // P1 (Top-Left)
            float x1 = radius * sin(b1) * sin(a1);
            float y1 = radius * cos(b1);
            float z1 = radius * sin(b1) * cos(a1);

            // P2 (Top-Right)
            float x2 = radius * sin(b1) * sin(a2);
            float y2 = radius * cos(b1);
            float z2 = radius * sin(b1) * cos(a2);

            // P3 (Bottom-Left)
            float x3 = radius * sin(b2) * sin(a1);
            float y3 = radius * cos(b2);
            float z3 = radius * sin(b2) * cos(a1);

            // P4 (Bottom-Right)
            float x4 = radius * sin(b2) * sin(a2);
            float y4 = radius * cos(b2);
            float z4 = radius * sin(b2) * cos(a2);

            float nx1 = x1 / radius, ny1 = y1 / radius, nz1 = z1 / radius;
            float nx2 = x2 / radius, ny2 = y2 / radius, nz2 = z2 / radius;
            float nx3 = x3 / radius, ny3 = y3 / radius, nz3 = z3 / radius;
            float nx4 = x4 / radius, ny4 = y4 / radius, nz4 = z4 / radius;

            float tu1 = (float)i / (float)slices;
            float tu2 = (float)(i + 1) / (float)slices;
            float tv1 = 1.0f - ((float)j / (float)stacks);
            float tv2 = 1.0f - ((float)(j + 1) / (float)stacks);

            // NORTH POLE (j == 0): Only one triangle needed (P1, P3, P4)
            // Note: P1 and P2 are the same point (0, R, 0)
            if (j == 0) {
                vertices.push_back({x1, y1, z1, nx1, ny1, nz1, tu1, tv1}); // Pole
                vertices.push_back({x3, y3, z3, nx3, ny3, nz3, tu1, tv2});
                vertices.push_back({x4, y4, z4, nx4, ny4, nz4, tu2, tv2});
            }
            // SOUTH POLE (j == stacks - 1): Only one triangle needed (P1, P4, P2)
            // Note: P3 and P4 are the same point (0, -R, 0)
            else if (j == stacks - 1) {
                vertices.push_back({x1, y1, z1, nx1, ny1, nz1, tu1, tv1});
                vertices.push_back({x4, y4, z4, nx4, ny4, nz4, tu2, tv2}); // Pole
                vertices.push_back({x2, y2, z2, nx2, ny2, nz2, tu2, tv1});
            }
            // BODY: Two triangles forming a quad
            else {
                // Triangle 1
                vertices.push_back({x1, y1, z1, nx1, ny1, nz1, tu1, tv1});
                vertices.push_back({x3, y3, z3, nx3, ny3, nz3, tu1, tv2});
                vertices.push_back({x4, y4, z4, nx4, ny4, nz4, tu2, tv2});

                // Triangle 2
                vertices.push_back({x1, y1, z1, nx1, ny1, nz1, tu1, tv1});
                vertices.push_back({x4, y4, z4, nx4, ny4, nz4, tu2, tv2});
                vertices.push_back({x2, y2, z2, nx2, ny2, nz2, tu2, tv1});
            }
        }
    }

    return vertices;
}
