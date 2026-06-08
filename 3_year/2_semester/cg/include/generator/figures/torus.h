/**
 * @file torus.h
 * @brief Definition of the Torus class for generating 3D torus figures.
 */
#ifndef TORUS_H
#define TORUS_H

#include "figure.h"
#include <vector>

/**
 * @class Torus
 * @brief Class representing a 3D torus, inheriting from Figure.
 */
class Torus : public Figure {
private:
    float innerRadius; /**< The inner radius (tube radius) of the torus. */
    float outerRadius; /**< The outer radius (distance from the center of the hole to the center of the tube). */
    int slices;        /**< Number of segments along the tube. */
    int stacks;        /**< Number of segments around the tube. */

public:
    /**
     * @brief Constructor for the Torus class.
     * @param inR The inner radius (radius of the tube itself).
     * @param outR The outer radius (distance from center to the tube center).
     * @param sl The number of slices.
     * @param st The number of stacks.
     */
    Torus(float inR, float outR, int sl, int st);

    /**
     * @brief Generates the vertices, normals, and texture coordinates for the torus.
     * @return A vector of Vertex objects representing the generated torus.
     */
    std::vector<Vertex> generate() override;
};

#endif
