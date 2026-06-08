/**
 * @file plane.h
 * @brief Definition of the Plane class for generating 3D plane figures.
 */
#ifndef PLANE_H
#define PLANE_H

#include "figure.h"

using namespace std;

/**
 * @class Plane
 * @brief Class representing a 3D plane, inheriting from Figure.
 */
class Plane : public Figure {
    private:
        float length;  /**< Length of the plane's sides. */
        int divisions; /**< Number of grid divisions along each side. */
    
    public:
        /**
         * @brief Constructor for the Plane class.
         * @param l The length of the plane.
         * @param d The number of divisions for the grid.
         */
        Plane(float l, int d);

        /**
         * @brief Generates the vertices, normals, and texture coordinates for the plane.
         * @return A vector of Vertex objects representing the generated plane.
         */
        vector<Vertex> generate() override;
};

#endif
