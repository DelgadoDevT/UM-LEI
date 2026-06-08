#ifndef FREE_CAMERA_H
#define FREE_CAMERA_H

#include "engine/Camera.h"

/**
 * @brief A free-flying camera that can be controlled via keyboard and mouse.
 * 
 * This class provides a first-person-like free camera which allows
 * moving in all directions and changing the view angle freely.
 */
class FreeCamera {
private:
    /** @brief X position of the free camera. */
    float px;
    /** @brief Y position of the free camera. */
    float py;
    /** @brief Z position of the free camera. */
    float pz;

    /** @brief Horizontal viewing angle. */
    float yaw;
    /** @brief Vertical viewing angle. */
    float pitch;
    
    /** @brief Movement speed. */
    float speed;
    /** @brief Keyboard look sensitivity. */
    float sensitivity;

    /** @brief Indicates if the mouse is currently being dragged. */
    bool dragging;
    /** @brief Last recorded X position of the mouse. */
    int lastX;
    /** @brief Last recorded Y position of the mouse. */
    int lastY;
    
    /** @brief Mouse movement look sensitivity. */
    float mouseSensitivity;
    /** @brief Mouse scroll zoom/movement speed. */
    float scrollSpeed;

public:
    /**
     * @brief Constructor for FreeCamera.
     * 
     * Initializes the free camera state based on an existing Camera.
     * 
     * @param initialCam The initial camera state to sync from.
     */
    FreeCamera(const Camera& initialCam);

    /**
     * @brief Updates the provided Camera object with the free camera's current state.
     * 
     * @param cam The Camera object to update.
     */
    void update(Camera& cam) const;

    /**
     * @brief Handles standard keyboard inputs for movement (W, A, S, D).
     * 
     * @param key The key pressed.
     */
    void handleKeys(unsigned char key);

    /**
     * @brief Handles special keyboard inputs for looking around (Arrow keys).
     * 
     * @param key The special key pressed.
     */
    void handleSpecialKeys(int key);

    /**
     * @brief Synchronizes the free camera's internal state with a given Camera.
     * 
     * @param cam The Camera object to sync state from.
     */
    void syncFrom(const Camera& cam);

    /**
     * @brief Handles mouse button press and release events.
     * 
     * @param button The mouse button (e.g., GLUT_LEFT_BUTTON).
     * @param state The state of the button (e.g., GLUT_DOWN).
     * @param x The X coordinate of the mouse.
     * @param y The Y coordinate of the mouse.
     */
    void handleMouseButton(int button, int state, int x, int y);

    /**
     * @brief Handles mouse motion events while a button is held down.
     * 
     * @param x The current X coordinate of the mouse.
     * @param y The current Y coordinate of the mouse.
     */
    void handleMouseMotion(int x, int y);
};

#endif
