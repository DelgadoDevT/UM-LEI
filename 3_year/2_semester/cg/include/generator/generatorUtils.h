/**
 * @file generatorUtils.h
 * @brief Utility functions and definitions for the generator module.
 */
#ifndef GENERATOR_UTILS_H
#define GENERATOR_UTILS_H

#include <string>

using namespace std;

/**
 * @enum ShapeID
 * @brief Identifiers for different supported shape types.
 */
enum ShapeID {
    ID_PLANE,   /**< Identifier for a plane */
    ID_BOX,     /**< Identifier for a box */
    ID_SPHERE,  /**< Identifier for a sphere */
    ID_CONE,    /**< Identifier for a cone */
    ID_TORUS,   /**< Identifier for a torus */
    ID_PATCH,   /**< Identifier for a Bezier patch */
    ID_COMETA,  /**< Identifier for a comet shape */
    ID_UNKNOWN  /**< Identifier for an unknown shape */
};

/**
 * @brief Converts a string representation of a shape into its corresponding ShapeID.
 * @param shape The name of the shape as a string.
 * @return The corresponding ShapeID enum value.
 */
ShapeID getShapeID(const string& shape);

/**
 * @brief Replaces all commas in the given string with spaces.
 * @param str The string to modify.
 */
void replaceCommasWithSpaces(string& str);

/**
 * @brief Computes the value of the Bernstein polynomial.
 * @param i The index of the polynomial.
 * @param t The parameter t, typically in the range [0, 1].
 * @return The evaluated Bernstein polynomial value.
 */
float getBernsteinPolynomial(int i, float t);

/**
 * @brief Computes the derivative of the Bernstein polynomial.
 * @param i The index of the polynomial.
 * @param t The parameter t, typically in the range [0, 1].
 * @return The evaluated Bernstein polynomial derivative.
 */
float getBernsteinDerivative(int i, float t);

/**
 * @brief Computes the cross product of two 3D vectors.
 * @param ax X component of the first vector.
 * @param ay Y component of the first vector.
 * @param az Z component of the first vector.
 * @param bx X component of the second vector.
 * @param by Y component of the second vector.
 * @param bz Z component of the second vector.
 * @param rx Output X component of the resulting vector.
 * @param ry Output Y component of the resulting vector.
 * @param rz Output Z component of the resulting vector.
 */
void crossProduct(float ax, float ay, float az, float bx, float by, float bz, float &rx, float &ry, float &rz);

/**
 * @brief Normalizes a 3D vector in place.
 * @param x The X component of the vector (will be normalized).
 * @param y The Y component of the vector (will be normalized).
 * @param z The Z component of the vector (will be normalized).
 */
void normalize(float &x, float &y, float &z);

#endif
