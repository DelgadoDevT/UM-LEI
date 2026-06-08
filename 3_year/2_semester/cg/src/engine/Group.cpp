#include "engine/Group.h"
#include <IL/il.h>
#include <iostream>
#include <string>

#ifdef __APPLE__
#include <GLUT/glut.h>
#else
#include <GL/glut.h>
#endif

using namespace tinyxml2;
extern bool debugNormalsActive;

/**
 * @brief Default constructor for Group.
 */
Group::Group() {
    // Empty
}

/**
 * @brief Loads the group's data (transforms, models, and children) from an XML element.
 * 
 * Parses the given XML element, extracting translation, rotation, scale,
 * models (with colors and textures), and recursively loads child groups.
 * 
 * @param groupElement Pointer to the tinyxml2::XMLElement representing this group.
 */
void Group::loadFromXML(XMLElement* groupElement) {
    if (!groupElement) return;
    bool hasCometModel = false;

    XMLElement* transformElement = groupElement->FirstChildElement("transform");
    if (transformElement) {
        XMLElement* tNode = transformElement->FirstChildElement();
        while (tNode) {
            std::string name = tNode->Name();
            
            if (name == "translate") {
                if (tNode->Attribute("time")) {
                    float time = tNode->FloatAttribute("time", 0.0f);
                    bool align = tNode->BoolAttribute("align", false);
                    std::vector<Point> points;
                    
                    XMLElement* pNode = tNode->FirstChildElement("point");
                    while (pNode) {
                        float px = pNode->FloatAttribute("x", 0.0f);
                        float py = pNode->FloatAttribute("y", 0.0f);
                        float pz = pNode->FloatAttribute("z", 0.0f);
                        points.push_back(Point(px, py, pz));
                        pNode = pNode->NextSiblingElement("point");
                    }
                    transforms.push_back(Transform(time, align, points));
                } else {
                    float x = tNode->FloatAttribute("x", 0.0f);
                    float y = tNode->FloatAttribute("y", 0.0f);
                    float z = tNode->FloatAttribute("z", 0.0f);
                    transforms.push_back(Transform(TransformType::TRANSLATE, x, y, z));
                }
            } 
            else if (name == "rotate") {
                float x = tNode->FloatAttribute("x", 0.0f);
                float y = tNode->FloatAttribute("y", 0.0f);
                float z = tNode->FloatAttribute("z", 0.0f);

                if (tNode->Attribute("time")) {
                    float time = tNode->FloatAttribute("time", 0.0f);
                    transforms.push_back(Transform(time, x, y, z));
                } else {
                    float angle = tNode->FloatAttribute("angle", 0.0f);
                    transforms.push_back(Transform(TransformType::ROTATE, x, y, z, angle));
                }
            } 
            else if (name == "scale") {
                float x = tNode->FloatAttribute("x", 1.0f);
                float y = tNode->FloatAttribute("y", 1.0f);
                float z = tNode->FloatAttribute("z", 1.0f);
                transforms.push_back(Transform(TransformType::SCALE, x, y, z));
            }
            tNode = tNode->NextSiblingElement();
        }
    }

    XMLElement* modelsElement = groupElement->FirstChildElement("models");
    if (modelsElement) {
        XMLElement* modelNode = modelsElement->FirstChildElement("model");
        while (modelNode) {
            const char* fileAttr = modelNode->Attribute("file");
            if (fileAttr) {
                const std::string modelFile = fileAttr;
                if (modelFile.find("cometa.3d") != std::string::npos) {
                    hasCometModel = true;
                }

                Model m;
                m.loadFromFile(fileAttr);

                XMLElement* colorNode = modelNode->FirstChildElement("color");
                if (colorNode) {
                    m.hasColor = true;

                    XMLElement* diffNode = colorNode->FirstChildElement("diffuse");
                    if (diffNode) {
                        m.diffuse[0] = diffNode->FloatAttribute("R", 200) / 255.0f;
                        m.diffuse[1] = diffNode->FloatAttribute("G", 200) / 255.0f;
                        m.diffuse[2] = diffNode->FloatAttribute("B", 200) / 255.0f;
                    }

                    XMLElement* ambNode = colorNode->FirstChildElement("ambient");
                    if (ambNode) {
                        m.ambient[0] = ambNode->FloatAttribute("R", 50) / 255.0f;
                        m.ambient[1] = ambNode->FloatAttribute("G", 50) / 255.0f;
                        m.ambient[2] = ambNode->FloatAttribute("B", 50) / 255.0f;
                    }

                    XMLElement* specNode = colorNode->FirstChildElement("specular");
                    if (specNode) {
                        m.specular[0] = specNode->FloatAttribute("R", 0) / 255.0f;
                        m.specular[1] = specNode->FloatAttribute("G", 0) / 255.0f;
                        m.specular[2] = specNode->FloatAttribute("B", 0) / 255.0f;
                    }

                    XMLElement* emiNode = colorNode->FirstChildElement("emissive");
                    if (emiNode) {
                        m.emissive[0] = emiNode->FloatAttribute("R", 0) / 255.0f;
                        m.emissive[1] = emiNode->FloatAttribute("G", 0) / 255.0f;
                        m.emissive[2] = emiNode->FloatAttribute("B", 0) / 255.0f;
                    }

                    XMLElement* shineNode = colorNode->FirstChildElement("shininess");
                    if (shineNode) {
                        m.shininess = shineNode->FloatAttribute("value", 0.0f);
                    }
                }

                XMLElement* textureNode = modelNode->FirstChildElement("texture");
                if (textureNode) {
                    const char* texFile = textureNode->Attribute("file");
                    if (texFile) {
                        m.textureID = Model::loadTexture(texFile);
                        if (m.textureID > 0) {
                            m.hasTexture = true;
                        }
                    }
                }

                models.push_back(m);
            }
            modelNode = modelNode->NextSiblingElement("model");
        }
    }

    if (hasCometModel) {
        for (auto& t : transforms) {
            if (t.type == TransformType::TRANSLATE_DYNAMIC) {
                t.isCometOrbit = true;
            }
        }
    }

    XMLElement* childGroupElement = groupElement->FirstChildElement("group");
    while (childGroupElement) {
        Group childGroup;
        childGroup.loadFromXML(childGroupElement);
        children.push_back(childGroup);
        childGroupElement = childGroupElement->NextSiblingElement("group");
    }
}

/**
 * @brief Draws the group and all its children.
 * 
 * Applies the transformations, renders the models, and recursively calls
 * draw() on all child groups. Wraps operations in glPushMatrix and glPopMatrix.
 */
void Group::draw() const {
    glPushMatrix(); 

    for (const auto& t : transforms) {
        t.apply();
    }

    for (const auto& m : models) {
        const_cast<Model&>(m).draw(); 
        if (debugNormalsActive)
            m.drawNormals();
    }

    for (const auto& child : children) {
        child.draw();
    }

    glPopMatrix();
}