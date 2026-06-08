/**
 * @file cone.h
 * @brief Definition of the Cone class for generating 3D cone figures.
 */
#ifndef CONE_H
#define CONE_H

#define _USE_MATH_DEFINES
#include <cmath>
#include "figure.h"

/**
 * @class Cone
 * @brief Class representing a 3D cone, inheriting from Figure.
 */
class Cone : public Figure {
    private:
        float radius; /**< Radius of the cone's base. */
        float height; /**< Height of the cone. */
        int slices;   /**< Number of slices around the cone's vertical axis. */
        int stacks;   /**< Number of vertical divisions (stacks) along the height. */
    
    public:
        /**
         * @brief Constructor for the Cone class.
         * @param r The radius of the cone base.
         * @param h The height of the cone.
         * @param sl The number of radial slices.
         * @param st The number of vertical stacks.
         */
        Cone(float r, float h, int sl, int st);

        /**
         * @brief Generates the vertices, normals, and texture coordinates for the cone.
         * @return A vector of Vertex objects representing the generated cone.
         */
        vector<Vertex> generate() override;
};

#endif
