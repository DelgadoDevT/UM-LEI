/**
 * @file cometa.h
 * @brief Definition of the Cometa class for generating 3D comet shapes.
 */
#ifndef COMETA_H
#define COMETA_H

#include "figure.h"
#include <vector>

/**
 * @class Cometa
 * @brief Class representing a procedural 3D comet shape, inheriting from Figure.
 */
class Cometa : public Figure {
private:
    float radius;    /**< Base radius of the comet. */
    int slices;      /**< Number of longitudinal slices. */
    int stacks;      /**< Number of latitudinal stacks. */
    int seed;        /**< Random seed used for procedural generation. */
    float roughness; /**< Factor controlling the surface displacement/roughness. */

public:
    /**
     * @brief Constructor for the Cometa class.
     * @param r Base radius of the comet.
     * @param sl Number of slices.
     * @param st Number of stacks.
     * @param sd Random seed for surface displacement.
     * @param rough Surface roughness/displacement factor.
     */
    Cometa(float r, int sl, int st, int sd, float rough);

    /**
     * @brief Generates the vertices, normals, and texture coordinates for the comet.
     * @return A vector of Vertex objects representing the generated comet.
     */
    std::vector<Vertex> generate() override;
};

#endif
