#ifndef ORBITAL_CAMERA_H
#define ORBITAL_CAMERA_H

#include "engine/Camera.h"

/**
 * @brief An orbital (spherical) camera that rotates around a central focal point.
 * 
 * Uses spherical coordinates (alpha, beta, radius) to orbit around the origin
 * or a target, useful for inspecting objects from all angles.
 */
class OrbitalCamera {
private:
    /** @brief The horizontal angle (longitude) in radians. */
    float alpha;  
    
    /** @brief The vertical angle (latitude) in radians. */
    float beta;   
    
    /** @brief The distance from the center (focal point). */
    float radius; 

public:
    /**
     * @brief Constructor for OrbitalCamera.
     * 
     * Converts the initial Cartesian coordinates of the camera to spherical coordinates.
     * 
     * @param initialCam The initial camera state.
     */
    OrbitalCamera(const Camera& initialCam);

    /**
     * @brief Updates the given Camera object based on the current spherical coordinates.
     * 
     * @param cam The Camera object to update.
     */
    void update(Camera& cam) const;

    /**
     * @brief Handles standard keyboard inputs to adjust the radius (zoom) and alpha.
     * 
     * @param key The key pressed.
     */
    void handleKeys(unsigned char key);

    /**
     * @brief Handles special keyboard inputs to orbit (adjust alpha and beta).
     * 
     * @param key The special key pressed.
     */
    void handleSpecialKeys(int key);
};

#endif
