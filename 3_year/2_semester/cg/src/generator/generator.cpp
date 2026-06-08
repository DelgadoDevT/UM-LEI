/**
 * @file generator.cpp
 * @brief Main entry point for the 3D figure generator application.
 */
#include <iostream>
#include <string>
#include <vector>
#include "generatorUtils.h"
#include "plane.h"
#include "box.h"
#include "sphere.h"
#include "cone.h"
#include "torus.h"
#include "patch.h"
#include "cometa.h"

using namespace std;

/**
 * @brief Main function for the generator.
 * Parses command-line arguments to generate a specific 3D figure and save it to a file.
 * @param argc The number of command-line arguments.
 * @param argv The command-line arguments array.
 * @return 0 on success, non-zero on failure.
 */
int main(int argc, char** argv) {
    if (argc < 2) {
        return 1;
    }

    Figure* figure = nullptr;
    ShapeID type = getShapeID(argv[1]);

    switch (type) {
        case ID_PLANE:
            if (argc == 5) figure = new Plane(stof(argv[2]), stoi(argv[3]));
            break;

        case ID_BOX:
            if (argc == 5) figure = new Box(stof(argv[2]), stoi(argv[3]));
            break;

        case ID_SPHERE:
            if (argc == 6) figure = new Sphere(stof(argv[2]), stoi(argv[3]), stoi(argv[4]));
            break;

        case ID_CONE:
            if (argc == 7) figure = new Cone(stof(argv[2]), stof(argv[3]), stoi(argv[4]), stoi(argv[5]));
            break;

        case ID_TORUS:
            if (argc == 7) figure = new Torus(stof(argv[2]), stof(argv[3]), stoi(argv[4]), stoi(argv[5]));
            break;
            
        case ID_PATCH:
            if (argc == 5) figure = new Patch(argv[2], stoi(argv[3]));
            break;

        case ID_COMETA:
            if (argc == 8) figure = new Cometa(stof(argv[2]), stoi(argv[3]), stoi(argv[4]), stoi(argv[5]), stof(argv[6]));
            break;

        default:
            cerr << "Unknown shape: " << argv[1] << endl;
            return 1;
    }

    if (figure) {
        figure->writeToFile(argv[argc - 1]);
        delete figure;
    } else {
        cerr << "Incorrect number of arguments for " << argv[1] << endl;
        return 1;
    }

    return 0;
}
