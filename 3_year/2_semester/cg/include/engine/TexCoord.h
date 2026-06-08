#pragma once

/**
 * @brief Represents a 2D texture coordinate.
 */
struct TexCoord {
    /** @brief The U (horizontal) texture coordinate. */
    float u;
    
    /** @brief The V (vertical) texture coordinate. */
    float v;

    /**
     * @brief Constructs a TexCoord with given U and V values.
     * 
     * @param u The U coordinate (default 0).
     * @param v The V coordinate (default 0).
     */
    TexCoord(float u = 0, float v = 0) : u(u), v(v) {}
};
