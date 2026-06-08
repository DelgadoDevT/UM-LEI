cmake_minimum_required(VERSION 3.10)
project(Fase4_CG)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED True)

if(POLICY CMP0072)
  cmake_policy(SET CMP0072 NEW)
endif()

find_package(OpenGL REQUIRED)
include_directories(${OPENGL_INCLUDE_DIRS})

if (WIN32)
    set(TOOLKITS_FOLDER "${CMAKE_SOURCE_DIR}/toolkits" CACHE PATH "")
    
    include_directories(${TOOLKITS_FOLDER}/glut ${TOOLKITS_FOLDER}/glew)
    
    set(WINDOWS_LIBS 
        ${TOOLKITS_FOLDER}/glut/glut32.lib 
        ${TOOLKITS_FOLDER}/glew/glew32.lib
        opengl32.lib
        glu32.lib
    )
    
    if (EXISTS "${TOOLKITS_FOLDER}/glut/glut32.dll" AND EXISTS "${TOOLKITS_FOLDER}/glew/glew32.dll")
        file(COPY ${TOOLKITS_FOLDER}/glut/glut32.dll DESTINATION ${CMAKE_BINARY_DIR})
        file(COPY ${TOOLKITS_FOLDER}/glew/glew32.dll DESTINATION ${CMAKE_BINARY_DIR})
    endif()
else ()
    find_package(GLUT REQUIRED)
    find_package(GLEW REQUIRED)
    find_package(DevIL REQUIRED)
    include_directories(${GLUT_INCLUDE_DIR} ${GLEW_INCLUDE_DIRS} ${IL_INCLUDE_DIR})
endif ()

add_executable(generator 
    src/generator/generator.cpp
    src/generator/generatorUtils.cpp
    src/generator/figures/figure.cpp
    src/generator/figures/plane.cpp
    src/generator/figures/box.cpp
    src/generator/figures/sphere.cpp
    src/generator/figures/cone.cpp
    src/generator/figures/torus.cpp
    src/generator/figures/patch.cpp
    src/generator/figures/cometa.cpp
)

target_include_directories(generator PRIVATE include include/generator include/generator/figures)

add_executable(engine 
    src/engine/engine.cpp
    src/engine/Model.cpp
    src/engine/Camara.cpp
    src/engine/tinyxml2.cpp
    src/engine/Group.cpp
    src/engine/Transform.cpp
    src/engine/OrbitalCamera.cpp
    src/engine/FreeCamera.cpp
)

target_include_directories(engine PRIVATE include include/engine)

if (WIN32)
    target_link_libraries(engine PRIVATE ${OPENGL_LIBRARIES} ${WINDOWS_LIBS})
    target_link_libraries(generator PRIVATE ${OPENGL_LIBRARIES} ${WINDOWS_LIBS})
else ()
    target_link_libraries(engine PRIVATE OpenGL::GL OpenGL::GLU GLUT::GLUT GLEW::GLEW ${IL_LIBRARIES})
    target_link_libraries(generator PRIVATE GL GLU glut)
endif ()
