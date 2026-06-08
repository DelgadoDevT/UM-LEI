#ifndef GROUP_H
#define GROUP_H

#include <vector>
#include "engine/Transform.h"
#include "engine/Model.h"
#include "engine/tinyxml2.h"

/**
 * @brief Represents a hierarchical node in the scene graph.
 * 
 * A Group can contain a list of transformations, a list of models to draw,
 * and a list of child groups. When drawn, the transformations are applied
 * to the models and the children sequentially.
 */
class Group {
private:
    /** @brief The list of transformations to apply to this group. */
    std::vector<Transform> transforms;

    /** @brief The list of models contained in this group. */
    std::vector<Model> models;

    /** @brief The child groups nested within this group. */
    std::vector<Group> children;

public:
    /**
     * @brief Default constructor for Group.
     */
    Group();

    /**
     * @brief Loads the group's data (transforms, models, and children) from an XML element.
     * 
     * Parses the given XML element, extracting translation, rotation, scale,
     * models (with colors and textures), and recursively loads child groups.
     * 
     * @param groupElement Pointer to the tinyxml2::XMLElement representing this group.
     */
    void loadFromXML(tinyxml2::XMLElement* groupElement);
    
    /**
     * @brief Draws the group and all its children.
     * 
     * Applies the transformations, renders the models, and recursively calls
     * draw() on all child groups. Wraps operations in glPushMatrix and glPopMatrix.
     */
    void draw() const;
};

#endif