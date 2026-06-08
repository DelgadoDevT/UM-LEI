#include "engine/FreeCamera.h"
#include <cmath>

#ifdef __APPLE__
#include <GLUT/glut.h>
#else
#include <GL/glut.h>
#endif

/**
 * @brief Constructor for FreeCamera.
 * 
 * Initializes the free camera state based on an existing Camera.
 * 
 * @param initialCam The initial camera state to sync from.
 */
FreeCamera::FreeCamera(const Camera& initialCam)
    : speed(2.0f), sensitivity(0.05f),
      dragging(false), lastX(0), lastY(0),
      mouseSensitivity(0.003f), scrollSpeed(3.0f) {
    syncFrom(initialCam);
}

/**
 * @brief Synchronizes the free camera's internal state with a given Camera.
 * 
 * @param cam The Camera object to sync state from.
 */
void FreeCamera::syncFrom(const Camera& cam) {
    px = cam.position.x;
    py = cam.position.y;
    pz = cam.position.z;

    float dx = cam.lookAt.x - px;
    float dy = cam.lookAt.y - py;
    float dz = cam.lookAt.z - pz;

    float horizDist = sqrt(dx * dx + dz * dz);
    pitch = atan2(dy, horizDist);
    yaw = atan2(dx, dz);
}

/**
 * @brief Updates the provided Camera object with the free camera's current state.
 * 
 * @param cam The Camera object to update.
 */
void FreeCamera::update(Camera& cam) const {
    cam.position.x = px;
    cam.position.y = py;
    cam.position.z = pz;

    cam.lookAt.x = px + cos(pitch) * sin(yaw);
    cam.lookAt.y = py + sin(pitch);
    cam.lookAt.z = pz + cos(pitch) * cos(yaw);
}

/**
 * @brief Handles standard keyboard inputs for movement (W, A, S, D).
 * 
 * @param key The key pressed.
 */
void FreeCamera::handleKeys(unsigned char key) {
    float dx = cos(pitch) * sin(yaw);
    float dy = sin(pitch);
    float dz = cos(pitch) * cos(yaw);
    float rx = -cos(yaw);
    float rz = sin(yaw);

    switch (key) {
        case 'w': case 'W':
            px += dx * speed;
            py += dy * speed;
            pz += dz * speed;
            break;
        case 's': case 'S':
            px -= dx * speed;
            py -= dy * speed;
            pz -= dz * speed;
            break;
        case 'a': case 'A':
            px -= rx * speed;
            pz -= rz * speed;
            break;
        case 'd': case 'D':
            px += rx * speed;
            pz += rz * speed;
            break;
    }
}

/**
 * @brief Handles special keyboard inputs for looking around (Arrow keys).
 * 
 * @param key The special key pressed.
 */
void FreeCamera::handleSpecialKeys(int key) {
    switch (key) {
        case GLUT_KEY_RIGHT: yaw -= sensitivity; break;
        case GLUT_KEY_LEFT:  yaw += sensitivity; break;
        case GLUT_KEY_UP:    pitch += sensitivity; break;
        case GLUT_KEY_DOWN:  pitch -= sensitivity; break;
    }

    if (pitch > 1.5f) pitch = 1.5f;
    if (pitch < -1.5f) pitch = -1.5f;
}

/**
 * @brief Handles mouse button press and release events.
 * 
 * @param button The mouse button (e.g., GLUT_LEFT_BUTTON).
 * @param state The state of the button (e.g., GLUT_DOWN).
 * @param x The X coordinate of the mouse.
 * @param y The Y coordinate of the mouse.
 */
void FreeCamera::handleMouseButton(int button, int state, int x, int y) {
    if (button == GLUT_LEFT_BUTTON) {
        if (state == GLUT_DOWN) {
            dragging = true;
            lastX = x;
            lastY = y;
        } else {
            dragging = false;
        }
    }

    if (state == GLUT_DOWN) {
        float dx = cos(pitch) * sin(yaw);
        float dy = sin(pitch);
        float dz = cos(pitch) * cos(yaw);

        // Map scroll wheel (buttons 3 and 4) to movement
        if (button == 3) {
            px += dx * scrollSpeed;
            py += dy * scrollSpeed;
            pz += dz * scrollSpeed;
        } else if (button == 4) {
            px -= dx * scrollSpeed;
            py -= dy * scrollSpeed;
            pz -= dz * scrollSpeed;
        }
    }
}

/**
 * @brief Handles mouse motion events while a button is held down.
 * 
 * @param x The current X coordinate of the mouse.
 * @param y The current Y coordinate of the mouse.
 */
void FreeCamera::handleMouseMotion(int x, int y) {
    if (!dragging) return;

    int dx = x - lastX;
    int dy = y - lastY;
    lastX = x;
    lastY = y;

    yaw   += dx * mouseSensitivity;
    pitch -= dy * mouseSensitivity;

    if (pitch > 1.5f) pitch = 1.5f;
    if (pitch < -1.5f) pitch = -1.5f;
}
