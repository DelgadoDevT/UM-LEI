/**
 * @file figure.h
 * @brief Base class definition for all geometric figures.
 */
#ifndef FIGURE_H
#define FIGURE_H

#include <vector>
#include <string>

using namespace std;

/**
 * @struct Vertex
 * @brief Represents a vertex in 3D space with normals and texture coordinates.
 */
struct Vertex {
    float x, y, z;    /**< Spatial coordinates (Position) */
    float nx, ny, nz; /**< Normal vector (Lighting) */
    float tu, tv;     /**< Texture coordinates */
};

/**
 * @class Figure
 * @brief Abstract base class for generating and saving 3D geometric shapes.
 */
class Figure {
    protected: 
        /**
         * @brief Default constructor for Figure.
         */
        Figure() = default;
    
    public:
        /**
         * @brief Virtual destructor for Figure.
         */
        virtual ~Figure() = default;

        /**
         * @brief Generates the vertices for the geometric figure.
         * @return A vector of Vertex objects defining the figure.
         */
        virtual vector<Vertex> generate() = 0;

        /**
         * @brief Writes the generated vertices to a file using a legacy format.
         * @param filename The path and name of the file to write to.
         */
        void writeToFileLegacy(const string& filename);

        /**
         * @brief Writes the generated vertices to a file using the standard format.
         * @param filename The path and name of the file to write to.
         */
        void writeToFile(const string& filename);
};

#endif
