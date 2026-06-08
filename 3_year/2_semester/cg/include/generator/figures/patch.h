/**
 * @file patch.h
 * @brief Definition of the Patch class for generating Bezier patches.
 */
#ifndef PATCH_H
#define PATCH_H

#include "figure.h"
#include <string>
#include <vector>

using namespace std;

/**
 * @class Patch
 * @brief Class representing a 3D Bezier patch, inheriting from Figure.
 */
class Patch : public Figure {
private:
    string filename;  /**< File containing the patch control points. */
    int tessellation; /**< The level of tessellation for the patch. */

public:
    /**
     * @brief Constructor for the Patch class.
     * @param filename Path to the file containing Bezier patch data.
     * @param tessellation The number of divisions for tessellating the patch.
     */
    Patch(const string& filename, int tessellation);

    /**
     * @brief Generates the vertices, normals, and texture coordinates for the patch.
     * @return A vector of Vertex objects representing the generated Bezier patch.
     */
    vector<Vertex> generate() override;
};

#endif
