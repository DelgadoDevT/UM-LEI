/**
 * @file sphere.h
 * @brief Definition of the Sphere class for generating 3D sphere figures.
 */
#ifndef SPHERE_H
#define SPHERE_H

#define _USE_MATH_DEFINES
#include <cmath>
#include "figure.h"

/**
 * @class Sphere
 * @brief Class representing a 3D sphere, inheriting from Figure.
 */
class Sphere : public Figure {
    private:
        float radius; /**< Radius of the sphere. */
        int slices;   /**< Number of vertical slices (longitude). */
        int stacks;   /**< Number of horizontal stacks (latitude). */
    
    public:
        /**
         * @brief Constructor for the Sphere class.
         * @param r The radius of the sphere.
         * @param sl The number of slices.
         * @param st The number of stacks.
         */
        Sphere(float r, int sl, int st);

        /**
         * @brief Generates the vertices, normals, and texture coordinates for the sphere.
         * @return A vector of Vertex objects representing the generated sphere.
         */
        vector<Vertex> generate() override;
};

#endif
