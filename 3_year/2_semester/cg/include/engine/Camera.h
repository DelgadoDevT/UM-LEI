#ifndef CAMERA_H
#define CAMERA_H

#include "Point.h"

/**
 * @brief Represents a camera in the 3D environment.
 * 
 * This class stores the basic parameters required to set up a camera,
 * including its position, target (lookAt), up vector, and projection
 * settings such as field of view (FOV) and clipping planes.
 */
class Camera {
public:
    /** @brief The position of the camera in 3D space. */
    Point position;

    /** @brief The point the camera is looking at. */
    Point lookAt;

    /** @brief The up vector defining the camera's orientation. */
    Point up;

    /** @brief The field of view angle (in degrees). */
    float fov;

    /** @brief The distance to the near clipping plane. */
    float nearPlane;

    /** @brief The distance to the far clipping plane. */
    float farPlane;

    /**
     * @brief Applies the camera settings to the current OpenGL context.
     * 
     * Sets up the projection and modelview matrices based on the camera's
     * parameters and the provided window dimensions.
     * 
     * @param windowWidth The width of the viewport.
     * @param windowHeight The height of the viewport.
     */
    void apply(int windowWidth, int windowHeight); 
};

#endif
