/**
 * @file torus.cpp
 * @brief Implementation of the Torus class methods.
 */
#include "torus.h"
#include "generatorUtils.h"
#include <cmath>

using namespace std;

/**
 * @brief Constructor for the Torus class.
 * @param inR The inner radius (radius of the tube itself).
 * @param outR The outer radius (distance from center to the tube center).
 * @param sl The number of slices.
 * @param st The number of stacks.
 */
Torus::Torus(float inR, float outR, int sl, int st) {
    this->innerRadius = inR;
    this->outerRadius = outR;
    this->slices = sl;
    this->stacks = st;
}

/**
 * @brief Generates the vertices, normals, and texture coordinates for the torus.
 * @return A vector of Vertex objects representing the generated torus.
 */
vector<Vertex> Torus::generate() {
    vector<Vertex> vertices;

    float alphaStep = (2.0f * M_PI) / (float)slices;
    float betaStep = (2.0f * M_PI) / (float)stacks;

    for (int i = 0; i < slices; i++) {
        for (int j = 0; j < stacks; j++) {
            float a1 = i * alphaStep;
            float a2 = (i + 1) * alphaStep;

            float b1 = j * betaStep;
            float b2 = (j + 1) * betaStep;

            // Center of the tube for the current slices
            float cx1 = outerRadius * cos(a1);
            float cz1 = outerRadius * sin(a1);
            float cx2 = outerRadius * cos(a2);
            float cz2 = outerRadius * sin(a2);

            // Positions
            float x1 = (outerRadius + innerRadius * cos(b1)) * cos(a1);
            float y1 = innerRadius * sin(b1);
            float z1 = (outerRadius + innerRadius * cos(b1)) * sin(a1);

            float x2 = (outerRadius + innerRadius * cos(b1)) * cos(a2);
            float y2 = innerRadius * sin(b1);
            float z2 = (outerRadius + innerRadius * cos(b1)) * sin(a2);

            float x3 = (outerRadius + innerRadius * cos(b2)) * cos(a1);
            float y3 = innerRadius * sin(b2);
            float z3 = (outerRadius + innerRadius * cos(b2)) * sin(a1);

            float x4 = (outerRadius + innerRadius * cos(b2)) * cos(a2);
            float y4 = innerRadius * sin(b2);
            float z4 = (outerRadius + innerRadius * cos(b2)) * sin(a2);

            // Normals (Point - Tube Center)
            float nx1 = x1 - cx1, ny1 = y1, nz1 = z1 - cz1;
            float nx2 = x2 - cx2, ny2 = y2, nz2 = z2 - cz2;
            float nx3 = x3 - cx1, ny3 = y3, nz3 = z3 - cz1;
            float nx4 = x4 - cx2, ny4 = y4, nz4 = z4 - cz2;

            // Normalize
            normalize(nx1, ny1, nz1);
            normalize(nx2, ny2, nz2);
            normalize(nx3, ny3, nz3);
            normalize(nx4, ny4, nz4);

            float tu1 = (float)i / (float)slices;
            float tu2 = (float)(i + 1) / (float)slices;
            float tv1 = (float)j / (float)stacks;
            float tv2 = (float)(j + 1) / (float)stacks;

            vertices.push_back({x1, y1, z1, nx1, ny1, nz1, tu1, tv1});
            vertices.push_back({x3, y3, z3, nx3, ny3, nz3, tu1, tv2});
            vertices.push_back({x4, y4, z4, nx4, ny4, nz4, tu2, tv2});

            vertices.push_back({x1, y1, z1, nx1, ny1, nz1, tu1, tv1});
            vertices.push_back({x4, y4, z4, nx4, ny4, nz4, tu2, tv2});
            vertices.push_back({x2, y2, z2, nx2, ny2, nz2, tu2, tv1});
        }
    }

    return vertices;
}
