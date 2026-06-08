#include "engine/Model.h"
#include <IL/il.h>
#include <fstream>
#include <iostream>
#include <string>
#include <unordered_map>
#include <vector>

#ifdef __APPLE__
#include <GL/glew.h>
#include <GLUT/glut.h>
#else
#include <GL/glew.h>
#include <GL/glut.h>
#endif

/**
 * @brief Loads model data from a legacy custom file format.
 * 
 * @param filename Path to the 3D model file.
 */
void Model::loadFromFileLegacy(const std::string& filename) {
    vertices.clear();
    indices.clear();
    std::ifstream file(filename);
    
    if (!file.is_open()) {
        std::cout << "ERRO a abrir o ficheiro -> " << filename << std::endl;
        return;
    }
    
    int numPoints;
    file >> numPoints;

    Point p;
    while (file >> p.x >> p.y >> p.z) {
        vertices.push_back(p);
    }
    file.close();
    
    prepareData();
}

/**
 * @brief Loads model data (vertices, normals, textures, indices) from a custom file format.
 * 
 * @param filename Path to the 3D model file.
 */
void Model::loadFromFile(const std::string& filename) {
    vertices.clear();
    indices.clear();
    normals.clear();
    texCoords.clear();
    std::ifstream file(filename);
    
    if (!file.is_open()) {
        std::cout << "ERRO a abrir o ficheiro -> " << filename << std::endl;
        return;
    }
    
    std::string token;
    
    while (file >> token) {
        if (token == "v") {
            Point p, n;
            TexCoord t;
            file >> p.x >> p.y >> p.z >> n.x >> n.y >> n.z >> t.u >> t.v;
            vertices.push_back(p);
            normals.push_back(n);
            texCoords.push_back(t);
        }
        else if (token == "f") {
            unsigned int id1, id2, id3;
            file >> id1 >> id2 >> id3;
            
            indices.push_back(id1 - 1);
            indices.push_back(id2 - 1);
            indices.push_back(id3 - 1);
        } 
        else if (token == "#") {
            std::string discard;
            std::getline(file, discard);
        }
    }
    file.close();
    
    prepareData();
}

/**
 * @brief Prepares and uploads the geometry data to OpenGL VBOs.
 */
void Model::prepareData() {
    if (!vertices.empty()) {
        glGenBuffers(1, &vbo);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices.size() * sizeof(Point), vertices.data(), GL_STATIC_DRAW);
    }

    if (!indices.empty()) {
        glGenBuffers(1, &ibo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.size() * sizeof(unsigned int), indices.data(), GL_STATIC_DRAW);
    }

    if (!normals.empty()) {
        glGenBuffers(1, &normalsVbo);
        glBindBuffer(GL_ARRAY_BUFFER, normalsVbo);
        glBufferData(GL_ARRAY_BUFFER, normals.size() * sizeof(Point), normals.data(), GL_STATIC_DRAW);
    }

    if (!texCoords.empty()) {
        glGenBuffers(1, &texVbo);
        glBindBuffer(GL_ARRAY_BUFFER, texVbo);
        glBufferData(GL_ARRAY_BUFFER, texCoords.size() * sizeof(TexCoord), texCoords.data(), GL_STATIC_DRAW);
    }
}

/**
 * @brief Renders the model using its VBOs and material/texture properties.
 */
void Model::draw() const {
    if (vbo == 0) return;

    if (hasColor) {
        glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse);
        glMaterialfv(GL_FRONT, GL_AMBIENT, ambient);
        glMaterialfv(GL_FRONT, GL_SPECULAR, specular);
        glMaterialfv(GL_FRONT, GL_EMISSION, emissive);
        glMaterialf(GL_FRONT, GL_SHININESS, shininess);
    }

    if (hasTexture && textureID > 0) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureID);
        
        if (texVbo > 0) {
            glBindBuffer(GL_ARRAY_BUFFER, texVbo);
            glTexCoordPointer(2, GL_FLOAT, 0, 0);
            glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        }
    } else {
        glDisable(GL_TEXTURE_2D);
    }

    if (normalsVbo > 0) {
        glBindBuffer(GL_ARRAY_BUFFER, normalsVbo);
        glNormalPointer(GL_FLOAT, 0, 0);
        glEnableClientState(GL_NORMAL_ARRAY);
    }

    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glVertexPointer(3, GL_FLOAT, 0, 0);
    
    glEnableClientState(GL_VERTEX_ARRAY);
    glColor3f(1.0f, 1.0f, 1.0f);

    if (ibo > 0) {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glDrawElements(GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);
    } else {
        glDrawArrays(GL_TRIANGLES, 0, vertices.size());
    }

    glDisableClientState(GL_VERTEX_ARRAY);

    if (normalsVbo > 0) {
        glDisableClientState(GL_NORMAL_ARRAY);
    }

    if (hasTexture && textureID > 0) {
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisable(GL_TEXTURE_2D);
    }
}

/**
 * @brief Static helper to load an image file into an OpenGL texture.
 * 
 * Includes caching to prevent loading the same texture multiple times.
 * 
 * @param filename Path to the image file.
 * @return GLuint The OpenGL texture ID, or 0 if loading fails.
 */
GLuint Model::loadTexture(const std::string& filename) {
    static std::unordered_map<std::string, GLuint> textureCache;
    auto it = textureCache.find(filename);
    if (it != textureCache.end()) {
        return it->second;
    }

    unsigned int t, tw, th;
    unsigned char *texData;
    unsigned int texID;

    ilGenImages(1, &t);
    ilBindImage(t);
    if (!ilLoadImage((ILstring)filename.c_str())) {
        std::cerr << "Erro ao carregar a textura: " << filename << std::endl;
        ilDeleteImages(1, &t);
        return 0;
    }

    tw = ilGetInteger(IL_IMAGE_WIDTH);
    th = ilGetInteger(IL_IMAGE_HEIGHT);
    ilConvertImage(IL_RGBA, IL_UNSIGNED_BYTE);
    texData = ilGetData();

    glGenTextures(1, &texID);
    glBindTexture(GL_TEXTURE_2D, texID);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, tw, th, 0, GL_RGBA, GL_UNSIGNED_BYTE, texData);
    glGenerateMipmap(GL_TEXTURE_2D);

    glBindTexture(GL_TEXTURE_2D, 0);
    ilDeleteImages(1, &t);
    textureCache[filename] = texID;

    return texID;
}

/**
 * @brief Draws the normal vectors of the model as lines, for debugging purposes.
 */
void Model::drawNormals() const {
    // Desativar luzes e texturas para garantir que a linha é desenhada com a cor pura
    glDisable(GL_LIGHTING);
    glDisable(GL_TEXTURE_2D);

    glColor3f(1.0f, 0.0f, 1.0f); // Cor Magenta para destacar

    glBegin(GL_LINES);
    // Assumindo que tens vetores chamados 'vertices' e 'normals' na tua classe
    for (size_t i = 0; i < vertices.size(); i++) {
        // Ponto de origem (o vértice na superfície do modelo)
        glVertex3f(vertices[i].x, vertices[i].y, vertices[i].z);
        
        // Ponto de destino (vértice + normal escalada)
        float scale = 0.5f; // Ajusta este valor se a linha ficar muito grande ou pequena
        glVertex3f(
            vertices[i].x + (normals[i].x * scale), 
            vertices[i].y + (normals[i].y * scale), 
            vertices[i].z + (normals[i].z * scale)
        );
    }
    glEnd();

    // Reativar os estados
    glEnable(GL_LIGHTING);
    glEnable(GL_TEXTURE_2D);
}