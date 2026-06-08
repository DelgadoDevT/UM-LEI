/**
 * @file generatorUtils.cpp
 * @brief Implementation of utility functions for the generator module.
 */
#include "generatorUtils.h"
#include <algorithm>
#include <cmath>

/**
 * @brief Converts a string representation of a shape into its corresponding ShapeID.
 * @param name The name of the shape as a string.
 * @return The corresponding ShapeID enum value.
 */
ShapeID getShapeID(const string& name) {
    if (name == "plane")  return ID_PLANE;
    if (name == "box")    return ID_BOX;
    if (name == "sphere") return ID_SPHERE;
    if (name == "cone")   return ID_CONE;
    if (name == "torus")  return ID_TORUS;
    if (name == "patch")  return ID_PATCH;
    if (name == "cometa") return ID_COMETA;
    return ID_UNKNOWN;
}

/**
 * @brief Replaces all commas in the given string with spaces.
 * @param str The string to modify.
 */
void replaceCommasWithSpaces(string& str) {
    replace(str.begin(), str.end(), ',', ' ');
}

/**
 * @brief Computes the value of the Bernstein polynomial.
 * @param i The index of the polynomial.
 * @param t The parameter t, typically in the range [0, 1].
 * @return The evaluated Bernstein polynomial value.
 */
float getBernsteinPolynomial(int i, float t) {
    switch (i) {
        case 0: return pow(1.0f - t, 3);
        case 1: return 3.0f * t * pow(1.0f - t, 2);
        case 2: return 3.0f * pow(t, 2) * (1.0f - t);
        case 3: return pow(t, 3);
        default: return 0.0f;
    }
}

/**
 * @brief Computes the derivative of the Bernstein polynomial.
 * @param i The index of the polynomial.
 * @param t The parameter t, typically in the range [0, 1].
 * @return The evaluated Bernstein polynomial derivative.
 */
float getBernsteinDerivative(int i, float t) {
    if (i == 0) return -3.0f * (1.0f - t) * (1.0f - t);
    if (i == 1) return 3.0f * (1.0f - t) * (1.0f - t) - 6.0f * t * (1.0f - t);
    if (i == 2) return 6.0f * t * (1.0f - t) - 3.0f * t * t;
    if (i == 3) return 3.0f * t * t;
    return 0.0f;
}

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
void crossProduct(float ax, float ay, float az, float bx, float by, float bz, float &rx, float &ry, float &rz) {
    rx = ay * bz - az * by;
    ry = az * bx - ax * bz;
    rz = ax * by - ay * bx;
}

/**
 * @brief Normalizes a 3D vector in place.
 * @param x The X component of the vector (will be normalized).
 * @param y The Y component of the vector (will be normalized).
 * @param z The Z component of the vector (will be normalized).
 */
void normalize(float &x, float &y, float &z) {
    float length = sqrt(x * x + y * y + z * z);
    if (length > 0.0f) {
        x /= length;
        y /= length;
        z /= length;
    }
}
