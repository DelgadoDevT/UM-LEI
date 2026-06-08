#ifndef TRANSFORM_H
#define TRANSFORM_H

#include <vector>
#include "engine/Point.h"

/**
 * @brief Defines the type of transformation to be applied.
 */
enum class TransformType { 
    TRANSLATE, 
    ROTATE, 
    SCALE, 
    TRANSLATE_DYNAMIC, 
    ROTATE_DYNAMIC 
};

/**
 * @brief Represents a geometric transformation (translation, rotation, scaling) 
 *        applied to a group or model.
 * 
 * Supports both static transformations and dynamic (time-based) transformations.
 */
struct Transform {
    /** @brief The type of this transformation. */
    TransformType type;

    /** @brief The X component of the transformation vector or rotation axis. */
    float x;

    /** @brief The Y component of the transformation vector or rotation axis. */
    float y;

    /** @brief The Z component of the transformation vector or rotation axis. */
    float z;

    /** @brief The static rotation angle in degrees (only for static ROTATE). */
    float angle;

    /** @brief Flag specific for comet orbit rendering. */
    bool isCometOrbit = false;
    
    /** @brief The time taken to complete one full dynamic transformation cycle. */
    float time;

    /** @brief If true, align the object to its dynamic translation path (Catmull-Rom). */
    bool align;

    /** @brief The control points defining the path for dynamic translation. */
    std::vector<Point> controlPoints;

    /**
     * @brief Constructor for static transformations (Translate, Rotate, Scale).
     * 
     * @param t The type of transformation.
     * @param x The X value.
     * @param y The Y value.
     * @param z The Z value.
     * @param angle The angle (used only for ROTATE). Default is 0.0f.
     */
    Transform(TransformType t, float x, float y, float z, float angle = 0.0f);
    
    /**
     * @brief Constructor for dynamic translation.
     * 
     * @param time Time for one full cycle.
     * @param align Whether to align the object with the path.
     * @param points Control points for the Catmull-Rom curve.
     */
    Transform(float time, bool align, const std::vector<Point>& points);
    
    /**
     * @brief Constructor for dynamic rotation.
     * 
     * @param time Time for one full 360-degree rotation.
     * @param x X component of the rotation axis.
     * @param y Y component of the rotation axis.
     * @param z Z component of the rotation axis.
     */
    Transform(float time, float x, float y, float z);

    /**
     * @brief Applies this transformation to the current OpenGL matrix stack.
     */
    void apply() const;
};

#endif
