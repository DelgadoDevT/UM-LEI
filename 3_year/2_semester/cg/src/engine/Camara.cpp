#include "engine/Camera.h"
#include <GL/glut.h>

/**
 * @brief Applies the camera settings to the current OpenGL context.
 * 
 * Sets up the projection and modelview matrices based on the camera's
 * parameters and the provided window dimensions.
 * 
 * @param windowWidth The width of the viewport.
 * @param windowHeight The height of the viewport.
 */
void Camera::apply(int windowWidth, int windowHeight) {
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    
    float ratio = (windowWidth * 1.0f) / windowHeight;
    gluPerspective(fov, ratio, nearPlane, farPlane);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    gluLookAt(position.x, position.y, position.z,
              lookAt.x, lookAt.y, lookAt.z,
              up.x, up.y, up.z);
}
