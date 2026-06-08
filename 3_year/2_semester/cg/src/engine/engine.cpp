#include "engine/Camera.h"
#include "engine/Model.h"
#include "engine/Point.h"
#include "engine/Group.h"
#include "engine/tinyxml2.h"
#include "engine/OrbitalCamera.h"
#include "engine/FreeCamera.h"
#include <IL/il.h>
#include <iostream>
#include <vector>
#include <stdio.h>

#ifdef __APPLE__
#include <GLUT/glut.h>
#else
#include <GL/glut.h>
#endif

using namespace tinyxml2;

/** @brief Width of the window. */
int windowWidth = 512;

/** @brief Height of the window. */
int windowHeight = 512;

/** @brief The main camera used for rendering. */
Camera myCamera;

/** @brief The initial camera state saved at startup, used for resetting views. */
Camera initialCameraState;

/** @brief The root group containing all models and transformations in the scene. */
Group sceneRoot;

/** @brief Pointer to the orbital camera handler. */
OrbitalCamera* orbitalCam = nullptr;

/** @brief Pointer to the free camera handler. */
FreeCamera* freeCam = nullptr;

/** @brief Flag indicating if the current active camera is the orbital camera. */
bool isOrbitalMode = true;

/** @brief Flag indicating if normal vectors should be drawn for debugging. */
bool debugNormalsActive = false;

/** @brief Flag indicating if the coordinate axes should be shown. */
bool showAxes = true;

/** @brief Flag indicating if the comet orbit path should be rendered. */
bool showCometOrbit = true;

/**
 * @brief Structure representing a light source in the scene.
 */
struct Light {
    /** @brief The type of the light ("point", "directional", "spotlight"). */
    std::string type;
    
    /** @brief Position or direction vector. The 4th component determines if it's a point (1.0) or directional (0.0). */
    float pos[4] = {0.0f, 0.0f, 0.0f, 1.0f}; 
    
    /** @brief Direction vector for spotlight. */
    float spotDir[3] = {0.0f, -1.0f, 0.0f};  
    
    /** @brief Cutoff angle for spotlight. */
    float cutoff = 180.0f;                   
};

/** @brief List of all light sources in the scene. */
std::vector<Light> sceneLights;

/**
 * @brief Main rendering callback function.
 * 
 * Handles clearing buffers, updating camera, setting up lights, rendering the scene graph,
 * drawing reference axes, and calculating FPS.
 */
void renderScene() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // ==========================================
    // CÁLCULO DE FPS
    // ==========================================
    static int frameCount = 0;
    static int previousTime = 0;

    frameCount++;
    int currentTime = glutGet(GLUT_ELAPSED_TIME);

    // Se já passou 1 segundo (1000 milissegundos)
    if (currentTime - previousTime > 1000) {
        float fps = frameCount * 1000.0f / (currentTime - previousTime);
        
        previousTime = currentTime;
        frameCount = 0;

        char title[256];
        sprintf(title, "Normals and Texture Coordinates - Fase 4 | FPS: %.2f", fps);
        glutSetWindowTitle(title);
    }
    // ==========================================

    if (isOrbitalMode)
        orbitalCam->update(myCamera);
    else
        freeCam->update(myCamera);

    // 1. Posicionar a câmara
    myCamera.apply(windowWidth, windowHeight);

    // Posicionar Luzes na Cena
    for (size_t i = 0; i < sceneLights.size() && i < 8; i++) {
        glLightfv(GL_LIGHT0 + i, GL_POSITION, sceneLights[i].pos);
        
        if (sceneLights[i].type == "spotlight" || sceneLights[i].type == "spot") {
            glLightfv(GL_LIGHT0 + i, GL_SPOT_DIRECTION, sceneLights[i].spotDir);
            glLightf(GL_LIGHT0 + i, GL_SPOT_CUTOFF, sceneLights[i].cutoff);
        } else {
            glLightf(GL_LIGHT0 + i, GL_SPOT_CUTOFF, 180.0f); 
        }
    }

    if (showAxes) {
        // Desenhar eixos de referência
        glDisable(GL_LIGHTING);
        glBegin(GL_LINES);
            // eixo dos X
            glColor3f(1.0f, 0.0f, 0.0f);
            glVertex3f(-1000.0f, 0.0f, 0.0f);
            glVertex3f(1000.0f, 0.0f, 0.0f);

            // eixo dos Y
            glColor3f(0.0f, 1.0f, 0.0f);
            glVertex3f(0.0f, -1000.0f, 0.0f);
            glVertex3f(0.0f, 1000.0f, 0.0f);

            // eixo dos Z
            glColor3f(0.0f, 0.0f, 1.0f);
            glVertex3f(0.0f, 0.0f, -1000.0f);
            glVertex3f(0.0f, 0.0f, 1000.0f);
        glEnd();
        glEnable(GL_LIGHTING);
    }

    // 2. Desenhar toda a hierarquia de cena
    sceneRoot.draw();

    glutSwapBuffers();
}

/**
 * @brief Parses an XML file to configure the application's initial state.
 * 
 * Loads window dimensions, camera settings, lighting information, and
 * the entire scene hierarchy (models and transforms).
 * 
 * @param filepath Path to the XML configuration file.
 */
void loadXML(const char* filepath) {
    XMLDocument doc;
    XMLError err = doc.LoadFile(filepath);
    
    if (err != XML_SUCCESS) {
        std::cerr << "Erro ao carregar o ficheiro XML: " << filepath << std::endl;
        return;
    }

    // Aceder à tag root <world>
    XMLElement* world = doc.FirstChildElement("world");
    if (!world) {
        std::cerr << "XML Invalido: Tag <world> nao encontrada." << std::endl;
        return;
    }

    // 1. Ler as dimensões da janela
    XMLElement* window = world->FirstChildElement("window");
    if (window) {
        windowWidth = window->IntAttribute("width", 512);
        windowHeight = window->IntAttribute("height", 512);
    }

    // 2. Ler as configurações da câmara
    XMLElement* camera = world->FirstChildElement("camera");
    if (camera) {
        XMLElement* pos = camera->FirstChildElement("position");
        if (pos) {
            myCamera.position.x = pos->FloatAttribute("x");
            myCamera.position.y = pos->FloatAttribute("y");
            myCamera.position.z = pos->FloatAttribute("z");
        }

        XMLElement* lookAt = camera->FirstChildElement("lookAt");
        if (lookAt) {
            myCamera.lookAt.x = lookAt->FloatAttribute("x");
            myCamera.lookAt.y = lookAt->FloatAttribute("y");
            myCamera.lookAt.z = lookAt->FloatAttribute("z");
        }

        XMLElement* up = camera->FirstChildElement("up");
        if (up) {
            myCamera.up.x = up->FloatAttribute("x");
            myCamera.up.y = up->FloatAttribute("y");
            myCamera.up.z = up->FloatAttribute("z");
        }

        XMLElement* proj = camera->FirstChildElement("projection");
        if (proj) {
            myCamera.fov = proj->FloatAttribute("fov");
            myCamera.nearPlane = proj->FloatAttribute("near");
            myCamera.farPlane = proj->FloatAttribute("far");
        }
    }

    // 3. Ler as configurações da luz
    XMLElement* lightsElement = world->FirstChildElement("lights");
    if (lightsElement) {
        XMLElement* lightNode = lightsElement->FirstChildElement("light");
        while (lightNode) {
            Light l;
            const char* typeAttr = lightNode->Attribute("type");
            if (typeAttr) {
                l.type = typeAttr;
                
                if (l.type == "directional") {
                    l.pos[0] = lightNode->FloatAttribute("dirx", lightNode->FloatAttribute("dirX", 0.0f));
                    l.pos[1] = lightNode->FloatAttribute("diry", lightNode->FloatAttribute("dirY", 0.0f));
                    l.pos[2] = lightNode->FloatAttribute("dirz", lightNode->FloatAttribute("dirZ", 0.0f));
                    l.pos[3] = 0.0f; 
                } 
                else if (l.type == "point") {
                    l.pos[0] = lightNode->FloatAttribute("posx", lightNode->FloatAttribute("posX", 0.0f));
                    l.pos[1] = lightNode->FloatAttribute("posy", lightNode->FloatAttribute("posY", 0.0f));
                    l.pos[2] = lightNode->FloatAttribute("posz", lightNode->FloatAttribute("posZ", 0.0f));
                    l.pos[3] = 1.0f; 
                }
                else if (l.type == "spotlight" || l.type == "spot") {
                    l.pos[0] = lightNode->FloatAttribute("posx", lightNode->FloatAttribute("posX", 0.0f));
                    l.pos[1] = lightNode->FloatAttribute("posy", lightNode->FloatAttribute("posY", 0.0f));
                    l.pos[2] = lightNode->FloatAttribute("posz", lightNode->FloatAttribute("posZ", 0.0f));
                    l.pos[3] = 1.0f;
                    
                    l.spotDir[0] = lightNode->FloatAttribute("dirx", lightNode->FloatAttribute("dirX", 0.0f));
                    l.spotDir[1] = lightNode->FloatAttribute("diry", lightNode->FloatAttribute("dirY", 0.0f));
                    l.spotDir[2] = lightNode->FloatAttribute("dirz", lightNode->FloatAttribute("dirZ", 0.0f));
                    
                    l.cutoff = lightNode->FloatAttribute("cutoff", 45.0f);
                }
            }
            sceneLights.push_back(l);
            lightNode = lightNode->NextSiblingElement("light");
        }
    }

    // 4. Ler a hierarquia de modelos e transformações (O primeiro Grupo)
    XMLElement* groupElement = world->FirstChildElement("group");
    if (groupElement) {
        sceneRoot.loadFromXML(groupElement);
    } 
}

/**
 * @brief Keyboard event callback for normal keys.
 * 
 * Handles switching between camera modes, toggling axes, and dispatching
 * movement commands to the active camera.
 * 
 * @param key The key pressed.
 * @param x The mouse X coordinate.
 * @param y The mouse Y coordinate.
 */
void processKeys(unsigned char key, int x, int y) {
    if (key == 'm' || key == 'M') {
        isOrbitalMode = !isOrbitalMode;
        if (isOrbitalMode) {
            myCamera.lookAt = initialCameraState.lookAt;
            myCamera.up = initialCameraState.up;
        } else {
            freeCam->syncFrom(myCamera);
        }
    } else if (key == 'x' || key == 'X') {
        showAxes = !showAxes;
        showCometOrbit = !showCometOrbit;
    } else if (key == 'n' || key == 'N') {
        debugNormalsActive = !debugNormalsActive;
    } else if (isOrbitalMode) {
        orbitalCam->handleKeys(key);
    } else {
        freeCam->handleKeys(key);
    }
    glutPostRedisplay();
}

/**
 * @brief Keyboard event callback for special keys (arrows).
 * 
 * Dispatches view changes to the active camera.
 * 
 * @param key The special key pressed.
 * @param x The mouse X coordinate.
 * @param y The mouse Y coordinate.
 */
void processSpecialKeys(int key, int x, int y) {
    if (isOrbitalMode)
        orbitalCam->handleSpecialKeys(key);
    else
        freeCam->handleSpecialKeys(key);
    glutPostRedisplay();
}

/**
 * @brief Mouse button callback.
 * 
 * Dispatches click events to the free camera if it's currently active.
 * 
 * @param button The mouse button pressed/released.
 * @param state The state (down/up).
 * @param x The mouse X coordinate.
 * @param y The mouse Y coordinate.
 */
void processMouseButtons(int button, int state, int x, int y) {
    if (!isOrbitalMode) {
        freeCam->handleMouseButton(button, state, x, y);
        glutPostRedisplay();
    }
}

/**
 * @brief Mouse motion callback when a button is held.
 * 
 * Dispatches motion events to the free camera.
 * 
 * @param x The mouse X coordinate.
 * @param y The mouse Y coordinate.
 */
void processMouseMotion(int x, int y) {
    if (!isOrbitalMode) {
        freeCam->handleMouseMotion(x, y);
        glutPostRedisplay();
    }
}

/**
 * @brief Main program entry point.
 * 
 * Initializes GLUT, DevIL, and GLEW, loads the XML configuration file,
 * sets up OpenGL states (lighting, depth, cull face), and enters the GLUT main loop.
 * 
 * @param argc Argument count.
 * @param argv Argument vector.
 * @return int Exit status.
 */
int main(int argc, char** argv) {
    if(argc < 2) {
        std::cerr << "Uso: " << argv[0] << " <ficheiro_de_configuracao.xml>\n";
        return -1;
    }

    // 1. Iniciar o GLUT PRIMEIRO!
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_DEPTH | GLUT_DOUBLE | GLUT_RGBA);
    glutInitWindowPosition(100, 100);
    glutInitWindowSize(windowWidth, windowHeight);
    glutCreateWindow("Curves, Cubic Surfaces and VBOs - Fase 4");

    // Inicializar DevIL
    ilInit();
    ilEnable(IL_ORIGIN_SET);
    ilOriginFunc(IL_ORIGIN_LOWER_LEFT);

    // 2. Inicializar GLEW ANTES DE LER O XML!
    #ifndef __APPLE__
        glewInit();
    #endif

    // 3. Carregar configuração (que agora já consegue criar VBOs)
    loadXML(argv[1]);
    glutReshapeWindow(windowWidth, windowHeight);
    initialCameraState = myCamera;

    orbitalCam = new OrbitalCamera(myCamera);
    freeCam = new FreeCamera(myCamera);

    glEnable(GL_LIGHTING);
    glEnable(GL_RESCALE_NORMAL); 
    
    float amb[4] = { 1.0f, 1.0f, 1.0f, 1.0f };
    glLightModelfv(GL_LIGHT_MODEL_AMBIENT, amb);

    float white[4] = {1.0f, 1.0f, 1.0f, 1.0f};
    for (size_t i = 0; i < sceneLights.size() && i < 8; i++) {
        glEnable(GL_LIGHT0 + i);
        glLightfv(GL_LIGHT0 + i, GL_DIFFUSE, white);
        glLightfv(GL_LIGHT0 + i, GL_SPECULAR, white);
    }

    // 4. Registar as funções
    glutDisplayFunc(renderScene);
    glutIdleFunc(renderScene);
    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);

    glutKeyboardFunc(processKeys);
    glutSpecialFunc(processSpecialKeys);
    glutMouseFunc(processMouseButtons);
    glutMotionFunc(processMouseMotion);

    // 5. Arrancar o ciclo principal
    glutMainLoop();
    return 0;
}
