/**
 * @file cone.cpp
 * @brief Implementation of the Cone class methods.
 */
#include "cone.h"

using namespace std;

/**
 * @brief Constructor for the Cone class.
 * @param r The radius of the cone base.
 * @param h The height of the cone.
 * @param sl The number of radial slices.
 * @param st The number of vertical stacks.
 */
Cone::Cone(float r, float h, int sl, int st) {
    this->radius = r;
    this->height = h;
    this->slices = sl;
    this->stacks = st;
}

/**
 * @brief Generates the vertices, normals, and texture coordinates for the cone.
 * @return A vector of Vertex objects representing the generated cone.
 */
vector<Vertex> Cone::generate() {
    vector<Vertex> vertices;

    float alphaStep = (2.0f * M_PI) / (float)slices;
    
    float stackHeight = height / (float)stacks;
    float radiusStep = radius / (float)stacks;

    // normal calculation constants for the side body
    float L = sqrt(height * height + radius * radius);
    float ny_body = radius / L;

    for (int i = 0; i < slices; i++) {
        float a1 = i * alphaStep;
        float a2 = (i + 1) * alphaStep;

        float x1_base = radius * sin(a1);
        float z1_base = radius * cos(a1);
        float x2_base = radius * sin(a2);
        float z2_base = radius * cos(a2);

        // base texture coordinates
        float tu_c = 0.5f, tv_c = 0.5f;
        float tu1_b = 0.5f + 0.5f * sin(a1);
        float tv1_b = 0.5f + 0.5f * cos(a1);
        float tu2_b = 0.5f + 0.5f * sin(a2);
        float tv2_b = 0.5f + 0.5f * cos(a2);

        // base normals (pointing down Y-)
        vertices.push_back({0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, tu_c, tv_c});
        vertices.push_back({x2_base, 0.0f, z2_base, 0.0f, -1.0f, 0.0f, tu2_b, tv2_b});
        vertices.push_back({x1_base, 0.0f, z1_base, 0.0f, -1.0f, 0.0f, tu1_b, tv1_b});

        // body normals for current slice angles
        float nx1 = (height * sin(a1)) / L;
        float nz1 = (height * cos(a1)) / L;
        
        float nx2 = (height * sin(a2)) / L;
        float nz2 = (height * cos(a2)) / L;

        float tu1 = (float)i / (float)slices;
        float tu2 = (float)(i + 1) / (float)slices;

        for (int j = 0; j < stacks; j++) {
            float y_b = j * stackHeight;         
            float y_t = (j + 1) * stackHeight;   

            float r_b = radius - (j * radiusStep);       
            float r_t = radius - ((j + 1) * radiusStep); 

            float x_bl = r_b * sin(a1);
            float z_bl = r_b * cos(a1);
            float x_br = r_b * sin(a2);
            float z_br = r_b * cos(a2);

            float x_tl = r_t * sin(a1);
            float z_tl = r_t * cos(a1);
            float x_tr = r_t * sin(a2);
            float z_tr = r_t * cos(a2);

            float tv1 = (float)j / (float)stacks;
            float tv2 = (float)(j + 1) / (float)stacks;

            // Triangle 1 
            vertices.push_back({x_tl, y_t, z_tl, nx1, ny_body, nz1, tu1, tv2});
            vertices.push_back({x_bl, y_b, z_bl, nx1, ny_body, nz1, tu1, tv1});
            vertices.push_back({x_br, y_b, z_br, nx2, ny_body, nz2, tu2, tv1});

            // Triangle 2 
            vertices.push_back({x_br, y_b, z_br, nx2, ny_body, nz2, tu2, tv1});
            vertices.push_back({x_tr, y_t, z_tr, nx2, ny_body, nz2, tu2, tv2});
            vertices.push_back({x_tl, y_t, z_tl, nx1, ny_body, nz1, tu1, tv2});
        }
    }

    return vertices;
}
