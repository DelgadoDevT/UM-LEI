#ifndef MODEL_H 
#define MODEL_H

#include <vector>
#include <string>
#include <GL/glew.h>
#include "Point.h"
#include "TexCoord.h"

/**
 * @brief Represents a 3D model consisting of vertices, normals, and texture coordinates.
 * 
 * Handles loading geometry from a file, preparing it into OpenGL Vertex Buffer Objects (VBOs),
 * and rendering it. It also manages material properties and textures.
 */
class Model {
public:
    /** @brief List of vertex positions. */
    std::vector<Point> vertices;

    /** @brief List of indices defining the faces (triangles) of the model. */
    std::vector<unsigned int> indices;

    /** @brief List of normal vectors for lighting. */
    std::vector<Point> normals;

    /** @brief List of texture coordinates. */
    std::vector<TexCoord> texCoords;

    /** @brief OpenGL ID for the Vertex Buffer Object (vertices). */
    GLuint vbo = 0;

    /** @brief OpenGL ID for the Index Buffer Object. */
    GLuint ibo = 0;

    /** @brief OpenGL ID for the Normals VBO. */
    GLuint normalsVbo = 0;

    /** @brief OpenGL ID for the Texture Coordinates VBO. */
    GLuint texVbo = 0;

    /** @brief OpenGL ID for the loaded texture. */
    GLuint textureID = 0;

    /** @brief Indicates if the model has a custom color material defined. */
    bool hasColor = false;

    /** @brief Indicates if the model has an active texture. */
    bool hasTexture = false;

    /** @brief Diffuse material color (RGBA). */
    float diffuse[4]  = {0.8f, 0.8f, 0.8f, 1.0f};

    /** @brief Ambient material color (RGBA). */
    float ambient[4]  = {0.2f, 0.2f, 0.2f, 1.0f};

    /** @brief Specular material color (RGBA). */
    float specular[4] = {0.0f, 0.0f, 0.0f, 1.0f};

    /** @brief Emissive material color (RGBA). */
    float emissive[4] = {0.0f, 0.0f, 0.0f, 1.0f};

    /** @brief Shininess coefficient for specular highlights. */
    float shininess = 0.0f;

    /**
     * @brief Loads model data from a legacy custom file format.
     * 
     * @param filename Path to the 3D model file.
     */
    void loadFromFileLegacy(const std::string& filename);

    /**
     * @brief Loads model data (vertices, normals, textures, indices) from a custom file format.
     * 
     * @param filename Path to the 3D model file.
     */
    void loadFromFile(const std::string& filename);

    /**
     * @brief Prepares and uploads the geometry data to OpenGL VBOs.
     */
    void prepareData();

    /**
     * @brief Renders the model using its VBOs and material/texture properties.
     */
    void draw() const;

    /**
     * @brief Static helper to load an image file into an OpenGL texture.
     * 
     * Includes caching to prevent loading the same texture multiple times.
     * 
     * @param filename Path to the image file.
     * @return GLuint The OpenGL texture ID, or 0 if loading fails.
     */
    static GLuint loadTexture(const std::string& filename);

    /**
     * @brief Draws the normal vectors of the model as lines, for debugging purposes.
     */
    void drawNormals() const;
};

#endif
