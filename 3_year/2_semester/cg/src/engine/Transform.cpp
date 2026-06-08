#include "engine/Transform.h"
#include <cmath>
#include <iostream>
#include <vector>

#ifdef __APPLE__
#include <GLUT/glut.h>
#else
#include <GL/glut.h>
#endif

extern bool showCometOrbit;

/**
 * @brief Constructor for static transformations (Translate, Rotate, Scale).
 * 
 * @param t The type of transformation.
 * @param x The X value.
 * @param y The Y value.
 * @param z The Z value.
 * @param angle The angle (used only for ROTATE). Default is 0.0f.
 */
Transform::Transform(TransformType t, float x, float y, float z, float angle) {
    this->type = t;
    this->x = x;
    this->y = y;
    this->z = z;
    this->angle = angle;
}

/**
 * @brief Constructor for dynamic translation.
 * 
 * @param time Time for one full cycle.
 * @param align Whether to align the object with the path.
 * @param points Control points for the Catmull-Rom curve.
 */
Transform::Transform(float time, bool align, const std::vector<Point>& points) {
    this->type = TransformType::TRANSLATE_DYNAMIC;
    this->time = time;
    this->align = align;
    this->controlPoints = points;
}

/**
 * @brief Constructor for dynamic rotation.
 * 
 * @param time Time for one full 360-degree rotation.
 * @param x X component of the rotation axis.
 * @param y Y component of the rotation axis.
 * @param z Z component of the rotation axis.
 */
Transform::Transform(float time, float x, float y, float z) {
    this->type = TransformType::ROTATE_DYNAMIC;
    this->time = time;
    this->x = x;
    this->y = y;
    this->z = z;
}

/**
 * @brief Multiplies a 4x4 matrix by a 4-component vector.
 * 
 * @param m Pointer to the 4x4 matrix.
 * @param v Pointer to the 4-component vector.
 * @param res Pointer to the resulting 4-component vector.
 */
void multMatrixVector(float *m, float *v, float *res) {
    for (int j = 0; j < 4; ++j) {
        res[j] = 0;
        for (int k = 0; k < 4; ++k) {
            res[j] += v[k] * m[j * 4 + k];
        }
    }
}

/**
 * @brief Calculates a point and its derivative on a Catmull-Rom curve.
 * 
 * @param t Interpolation parameter [0, 1) within the segment.
 * @param p0 Point 0 (control point).
 * @param p1 Point 1 (start of the segment).
 * @param p2 Point 2 (end of the segment).
 * @param p3 Point 3 (control point).
 * @param pos Output parameter for the resulting position.
 * @param deriv Output parameter for the resulting derivative.
 */
void getCatmullRomPoint(float t, Point p0, Point p1, Point p2, Point p3, float *pos, float *deriv) {
    // catmull-rom matrix
    float m[4][4] = { {-0.5f,  1.5f, -1.5f,  0.5f},
                      { 1.0f, -2.5f,  2.0f, -0.5f},
                      {-0.5f,  0.0f,  0.5f,  0.0f},
                      { 0.0f,  1.0f,  0.0f,  0.0f} };

    for (int i = 0; i < 3; ++i) {
        float p[4] = { p0.get(i), p1.get(i), p2.get(i), p3.get(i) };
        float a[4];

        // Compute A = M * P
        multMatrixVector((float*)m, p, a);

        // Compute pos = T * A
        if (pos) pos[i] = pow(t, 3) * a[0] + pow(t, 2) * a[1] + t * a[2] + a[3];

        // compute deriv = T' * A
        if (deriv) deriv[i] = 3 * pow(t, 2) * a[0] + 2 * t * a[1] + a[2];
    }
}

/**
 * @brief Given a global t value, calculates the global position and derivative on the entire Catmull-Rom curve.
 * 
 * @param gt Global interpolation parameter [0, 1) over the whole curve.
 * @param controlPoints The set of control points defining the curve.
 * @param pos Output parameter for the resulting position.
 * @param deriv Output parameter for the resulting derivative.
 */
void getGlobalCatmullRomPoint(float gt, const std::vector<Point>& controlPoints, float *pos, float *deriv) {
    int pointCount = controlPoints.size();
    float t = gt * pointCount; // this is the real global t
    int index = floor(t);  // which segment
    t = t - index; // where within the segment

    // indices store the points
    int indices[4];
    indices[0] = (index + pointCount - 1) % pointCount;
    indices[1] = (indices[0] + 1) % pointCount;
    indices[2] = (indices[1] + 1) % pointCount;
    indices[3] = (indices[2] + 1) % pointCount;

    getCatmullRomPoint(t, controlPoints[indices[0]], controlPoints[indices[1]], controlPoints[indices[2]], controlPoints[indices[3]], pos, deriv);
}

/**
 * @brief Builds a 4x4 rotation matrix given three orthogonal axis vectors.
 * 
 * @param x The X axis vector.
 * @param y The Y axis vector.
 * @param z The Z axis vector.
 * @param m Output parameter for the resulting 4x4 matrix.
 */
void buildRotationMatrix(float *x, float *y, float *z, float *m) {
    m[0] = x[0]; m[1] = x[1]; m[2] = x[2]; m[3] = 0;
    m[4] = y[0]; m[5] = y[1]; m[6] = y[2]; m[7] = 0;
    m[8] = z[0]; m[9] = z[1]; m[10] = z[2]; m[11] = 0;
    m[12] = 0; m[13] = 0; m[14] = 0; m[15] = 1;
}

/**
 * @brief Computes the cross product of two 3-component vectors.
 * 
 * @param a First vector.
 * @param b Second vector.
 * @param res Resulting cross product vector.
 */
void cross(float *a, float *b, float *res) {
    res[0] = a[1] * b[2] - a[2] * b[1];
    res[1] = a[2] * b[0] - a[0] * b[2];
    res[2] = a[0] * b[1] - a[1] * b[0];
}

/**
 * @brief Normalizes a 3-component vector in place.
 * 
 * @param a The vector to normalize.
 */
void normalize(float *a) {
    float l = sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
    if (l == 0) return;
    a[0] /= l;
    a[1] /= l;
    a[2] /= l;
}

/**
 * @brief Renders a visual representation of a Catmull-Rom curve (e.g., an orbit path).
 * 
 * @param controlPoints The control points defining the curve to render.
 */
void renderCatmullRomCurve(const std::vector<Point>& controlPoints) {
    float pos[3];
    float deriv[3];

    glBegin(GL_LINE_LOOP);
    glColor3f(0.5f, 0.5f, 0.5f); // Cor cinza para a linha da curva
    for (float gt = 0; gt < 1.0; gt += 0.01) {
        getGlobalCatmullRomPoint(gt, controlPoints, pos, deriv);
        glVertex3f(pos[0], pos[1], pos[2]);
    }
    glEnd();
}

/** @brief Stores the previous Y-axis for consistent alignment calculation. */
static float y_prev[3] = {0, 1, 0};

/**
 * @brief Applies this transformation to the current OpenGL matrix stack.
 */
void Transform::apply() const {
    float currentTime = glutGet(GLUT_ELAPSED_TIME) / 1000.0f;

    if (type == TransformType::TRANSLATE) {
        glTranslatef(x, y, z);
    }
    else if (type == TransformType::TRANSLATE_DYNAMIC) {
        if (controlPoints.size() < 4) return;

        // A órbita do cometa pode ser alternada com a tecla X.
        if (!isCometOrbit || showCometOrbit) {
            renderCatmullRomCurve(controlPoints);
        }

        float gt = fmod(currentTime, time) / time;
        float pos[3], deriv[3];
        getGlobalCatmullRomPoint(gt, controlPoints, pos, deriv);

        glTranslatef(pos[0], pos[1], pos[2]);

        if (align) {
            float X[3] = { deriv[0], deriv[1], deriv[2] };
            normalize(X);

            float Z[3];
            cross(X, y_prev, Z);
            normalize(Z);

            cross(Z, X, y_prev);
            normalize(y_prev);

            float m[16];
            buildRotationMatrix(X, y_prev, Z, m);
            glMultMatrixf(m);
        }
    }
    else if (type == TransformType::ROTATE) {
        glRotatef(angle, x, y, z);
    }
    else if (type == TransformType::ROTATE_DYNAMIC) {
        float currentAngle = (fmod(currentTime, time) / time) * 360.0f;
        glRotatef(currentAngle, x, y, z);
    }
    else if (type == TransformType::SCALE) {
        glScalef(x, y, z);
    }
}
