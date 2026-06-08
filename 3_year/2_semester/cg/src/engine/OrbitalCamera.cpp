#include "engine/OrbitalCamera.h"
#include <cmath>

#ifdef __APPLE__
#include <GLUT/glut.h>
#else
#include <GL/glut.h>
#endif

/**
 * @brief Constructor for OrbitalCamera.
 * 
 * Converts the initial Cartesian coordinates of the camera to spherical coordinates.
 * 
 * @param initialCam The initial camera state.
 */
OrbitalCamera::OrbitalCamera(const Camera& initialCam) {
    float px = initialCam.position.x;
    float py = initialCam.position.y;
    float pz = initialCam.position.z;

    radius = sqrt(px * px + py * py + pz * pz);
    
    if (radius < 0.1f) radius = 0.1f;

    beta = asin(py / radius);
    alpha = atan2(px, pz);
}

/**
 * @brief Updates the given Camera object based on the current spherical coordinates.
 * 
 * @param cam The Camera object to update.
 */
void OrbitalCamera::update(Camera& cam) const {
    cam.position.x = radius * cos(beta) * sin(alpha);
    cam.position.y = radius * sin(beta);
    cam.position.z = radius * cos(beta) * cos(alpha);
}

/**
 * @brief Handles standard keyboard inputs to adjust the radius (zoom) and alpha.
 * 
 * @param key The key pressed.
 */
void OrbitalCamera::handleKeys(unsigned char key) {
    switch(key) {
        case 'w': case 'W': radius -= 5.0f; break; // Zoom In
        case 's': case 'S': radius += 5.0f; break; // Zoom Out
        case 'd': case 'D': alpha += 0.1f; break;
        case 'a': case 'A': alpha -= 0.1f; break;
    }
    if (radius < 1.0f) radius = 1.0f;
}

/**
 * @brief Handles special keyboard inputs to orbit (adjust alpha and beta).
 * 
 * @param key The special key pressed.
 */
void OrbitalCamera::handleSpecialKeys(int key) {
    switch (key) {
        case GLUT_KEY_RIGHT: alpha += 0.1f; break;
        case GLUT_KEY_LEFT:  alpha -= 0.1f; break;
        case GLUT_KEY_UP:    beta += 0.1f;  break;
        case GLUT_KEY_DOWN:  beta -= 0.1f;  break;
    }

    if (beta > 1.5f) beta = 1.5f;
    if (beta < -1.5f) beta = -1.5f;
}
