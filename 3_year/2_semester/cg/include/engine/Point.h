#pragma once

/**
 * @brief Represents a point or vector in 3D space.
 */
struct Point {
    /** @brief X coordinate of the point. */
    float x;
    /** @brief Y coordinate of the point. */
    float y;
    /** @brief Z coordinate of the point. */
    float z;

    /**
     * @brief Constructs a Point with the given coordinates.
     * 
     * @param x X coordinate (default 0).
     * @param y Y coordinate (default 0).
     * @param z Z coordinate (default 0).
     */
    Point(float x = 0, float y = 0, float z = 0) : x(x), y(y), z(z) {}

    /**
     * @brief Gets the value of a specific axis.
     * 
     * @param axis The axis index (0 = x, 1 = y, 2 = z).
     * @return float The value of the corresponding axis.
     */
    float get(int axis) const {
        if (axis == 0) return x;
        if (axis == 1) return y;
        return z;
    }
};
