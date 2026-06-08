/**
 * @file patch.cpp
 * @brief Implementation of the Patch class methods for generating Bezier patches.
 */
#include "patch.h"
#include "generatorUtils.h"
#include <fstream>
#include <iostream>
#include <sstream>

using namespace std;

/**
 * @brief Constructor for the Patch class.
 * @param filename Path to the file containing Bezier patch data.
 * @param tessellation The number of divisions for tessellating the patch.
 */
Patch::Patch(const string& filename, int tessellation) {
    this->filename = filename;
    this->tessellation = tessellation;
}

/**
 * @brief Evaluates a point on a Bezier patch using the given parameters and control points.
 * @param u The u parameter [0, 1].
 * @param v The v parameter [0, 1].
 * @param indices A vector containing the 16 control point indices for the patch.
 * @param controlPoints A vector containing all the control points defined.
 * @return The evaluated Vertex at the given parameters.
 */
Vertex getBezierPoint(float u, float v, const vector<int>& indices, const vector<Vertex>& controlPoints) {
    float x = 0.0f, y = 0.0f, z = 0.0f;
    float tu_x = 0.0f, tu_y = 0.0f, tu_z = 0.0f;
    float tv_x = 0.0f, tv_y = 0.0f, tv_z = 0.0f;

    for (int i = 0; i < 4; i++) {
        float bu = getBernsteinPolynomial(i, u);
        float dbu = getBernsteinDerivative(i, u);
        
        for (int j = 0; j < 4; j++) {
            float bv = getBernsteinPolynomial(j, v);
            float dbv = getBernsteinDerivative(j, v);
            
            int pointIndex = indices[i * 4 + j];
            Vertex p = controlPoints[pointIndex];
            
            float weight = bu * bv;
            x += p.x * weight;
            y += p.y * weight;
            z += p.z * weight;
            
            float weight_du = dbu * bv;
            tu_x += p.x * weight_du;
            tu_y += p.y * weight_du;
            tu_z += p.z * weight_du;
            
            float weight_dv = bu * dbv;
            tv_x += p.x * weight_dv;
            tv_y += p.y * weight_dv;
            tv_z += p.z * weight_dv;
        }
    }

    float nx, ny, nz;
    
    crossProduct(tv_x, tv_y, tv_z, tu_x, tu_y, tu_z, nx, ny, nz);
    normalize(nx, ny, nz);

    return {x, y, z, nx, ny, nz, u, 1.0f - v};
}

/**
 * @brief Generates the vertices, normals, and texture coordinates for the patch.
 * @return A vector of Vertex objects representing the generated Bezier patch.
 */
vector<Vertex> Patch::generate() {
    vector<Vertex> result;
    ifstream file(filename);

    if (!file.is_open()) {
        cerr << "Erro: Nao foi possivel abrir o ficheiro patch " << filename << endl;
        return result;
    }

    string line;
    
    // Ler número de patches
    getline(file, line);
    int numPatches = stoi(line);
    vector<vector<int>> patchIndices(numPatches, vector<int>(16));

    // Ler índices dos patches
    for (int i = 0; i < numPatches; i++) {
        getline(file, line);
        replaceCommasWithSpaces(line);
        stringstream ss(line);
        for (int j = 0; j < 16; j++) {
            ss >> patchIndices[i][j];
        }
    }

    // Ler número de pontos de controlo
    getline(file, line);
    int numControlPoints = stoi(line);
    vector<Vertex> controlPoints(numControlPoints);

    // Ler coordenadas dos pontos de controlo
    for (int i = 0; i < numControlPoints; i++) {
        getline(file, line);
        replaceCommasWithSpaces(line);
        stringstream ss(line);
        ss >> controlPoints[i].x >> controlPoints[i].y >> controlPoints[i].z;
    }
    file.close();

    float step = 1.0f / tessellation;

    for (int p = 0; p < numPatches; p++) {
        for (int i = 0; i < tessellation; i++) {
            for (int j = 0; j < tessellation; j++) {
                float u1 = i * step;
                float v1 = j * step;
                float u2 = (i + 1) * step;
                float v2 = (j + 1) * step;

                Vertex p1 = getBezierPoint(u1, v1, patchIndices[p], controlPoints);
                Vertex p2 = getBezierPoint(u2, v1, patchIndices[p], controlPoints);
                Vertex p3 = getBezierPoint(u1, v2, patchIndices[p], controlPoints);
                Vertex p4 = getBezierPoint(u2, v2, patchIndices[p], controlPoints);

                result.push_back(p1);
                result.push_back(p3);
                result.push_back(p2);

                result.push_back(p2);
                result.push_back(p3);
                result.push_back(p4);            
            }
        }
    }
    return result;
}
