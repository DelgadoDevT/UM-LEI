/**
 * @file box.h
 * @brief Definition of the Box class for generating 3D box figures.
 */
#ifndef BOX_H
#define BOX_H

#include "figure.h"

/**
 * @class Box
 * @brief Class representing a 3D box, inheriting from Figure.
 */
class Box : public Figure {
    private:
        float units; /**< The size of the box (length of each side). */
        int grid;    /**< The number of grid subdivisions along each face. */
    
    public:
        /**
         * @brief Constructor for the Box class.
         * @param u The dimension (units) of the box edges.
         * @param g The number of grid divisions for the box faces.
         */
        Box(float u, int g);

        /**
         * @brief Generates the vertices, normals, and texture coordinates for the box.
         * @return A vector of Vertex objects representing the generated box.
         */
        vector<Vertex> generate() override;
};

#endif
