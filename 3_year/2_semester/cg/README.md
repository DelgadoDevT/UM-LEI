<div align="center">

# 🪐 CG-2526 - 3D Graphics Engine & Generator

### *A Custom OpenGL 3D Rendering Engine and Primitive Generator*

[![C++](https://img.shields.io/badge/C++-11-blue.svg)](https://isocpp.org/)
[![OpenGL](https://img.shields.io/badge/OpenGL-3.3-green.svg)](https://www.opengl.org/)
[![CMake](https://img.shields.io/badge/CMake-3.10-red.svg)](https://cmake.org/)
[![License](https://img.shields.io/badge/License-MIT-brightgreen.svg)](LICENSE)

---

**CG-2526** is a comprehensive Computer Graphics project featuring a custom **3D Engine** and a **Model Generator**. Built with C++ and OpenGL, it supports hierarchical scene graphs via XML, advanced camera systems, lighting, textures, Catmull-Rom animations, and VBO-based rendering.

[Features](#-key-features) • [Installation](#️-installation) • [Usage](#-usage) • [Demos & Tests](#-demos--tests) • [Project Structure](#-project-structure)

---

</div>

## ✨ Key Features

<table>
<tr>
<td width="50%">

### 🧊 3D Primitive Generator
- Generates standard primitives: `plane`, `box`, `sphere`, `cone`, `torus`
- Supports complex shapes like **Bézier Patches**
- Procedural generation (e.g., custom comets with seeds and roughness)
- Outputs optimized `.3d` format files

</td>
<td width="50%">

### 🎮 Advanced 3D Engine
- **Scene Graph Architecture:** Hierarchical XML-based configuration
- **Efficient Rendering:** Uses Vertex Buffer Objects (VBOs)
- **Dynamic Lighting & Materials:** Point, directional, and spot lights; Diffuse, specular, ambient, and emissive properties
- **Texture Mapping:** Integrated with DevIL

</td>
</tr>
<tr>
<td width="50%">

### 🎥 Camera System
- **Orbital Camera:** Rotate around the scene origin (Explorer mode)
- **Free Camera:** First-person style movement (FPS mode)
- Dynamic aspect ratio adjustment

</td>
<td width="50%">

### 🏃‍♂️ Animations
- **Catmull-Rom Splines:** Smooth curve translations using control points
- **Continuous Rotations:** Time-based transformations

</td>
</tr>
</table>

---

## 🛠️ Installation

### Prerequisites

- **C++11** compatible compiler
- **CMake** (>= 3.10)
- **OpenGL**, **GLUT**, **GLEW**, and **DevIL** (Image Library)

*On Linux (Ubuntu/Debian):*
```bash
sudo apt-get install cmake libgl1-mesa-dev libglu1-mesa-dev freeglut3-dev libglew-dev libdevil-dev
```

### Build the Project

1. **Clone the repository** and open a terminal in its root directory.

2. **Compile using CMake:**
```bash
mkdir -p build
cd build
cmake ..
make
```

This will generate two executables in the `build` directory: `generator` and `engine`.

### Build Documentation

The project uses Doxygen for code documentation. To generate the HTML documentation:

```bash
doxygen Doxyfile
```

The generated documentation will be available in the `docs` directory. Open `docs/index.html` in your browser.

---

## 🚀 Usage

### 1️⃣ Generator

The generator creates 3D models and exports them as `.3d` files.

```bash
# General syntax
./build/generator <primitive> [parameters] <output_file.3d>

# Examples:
./build/generator sphere 1 50 50 sphere.3d
./build/generator torus 0.25 1.5 60 60 torus.3d
./build/generator cometa 1 20 20 42 0.1 cometa.3d
./build/generator patch teapot.patch 10 teapot.3d
```

> **💡 Tip:** You can generate all basic primitives at once using the provided script:
> `./testes_normais/generate_all.sh`

### 2️⃣ Engine

The engine reads an XML configuration file to load and render the scene.

```bash
./build/engine <path_to_scene.xml>
```

---

## 🎬 Demos & Tests

### Solar System Demo 🪐

Experience a complete demonstration featuring planets, textures, orbits using Catmull-Rom curves, and lighting.

```bash
./demo_sistema_solar/run_demo.sh
```

### Phase-Specific Tests 🧪

The project was developed in 4 phases. You can run automated tests for any specific phase using the provided script:

```bash
# Usage: ./run_test.sh <phase_number> <test_number>
./test_files/run_test.sh 1 3
./test_files/run_test.sh 4 1
```

---

## 📂 Project Structure

```
CG-2526/
├── 📁 demo_sistema_solar/    # Complete Solar System demonstration
├── 📁 include/               # Header files (.h)
│   ├── 📁 engine/            # Engine headers (Camera, Group, XML, etc.)
│   └── 📁 generator/         # Generator headers (Primitives, Patches)
├── 📁 src/                   # Source files (.cpp)
│   ├── 📁 engine/            # Engine logic and rendering
│   └── 📁 generator/         # Model generation logic
├── 📁 test_files/            # XML scenes and patches for testing
├── 📁 testes_normais/        # XML scenes for testing
├── 📄 CMakeLists.txt         # Build configuration
└── 📄 README.md              # This file
```

---

## 👥 Authors

**Group 6** - *Computer Graphics Course*

<table>
<tr>
<td align="center">
<a href="https://github.com/CarlosAraujo05">
<img src="https://github.com/CarlosAraujo05.png" width="100px;" alt="CarlosAraujo05"/><br />
<sub><b>CarlosAraujo05</b></sub>
<td align="center">
<a href="https://github.com/DelgadoDevT">
<img src="https://github.com/DelgadoDevT.png" width="100px;" alt="DelgadoDevT"/><br />
<sub><b>DelgadoDevT</b></sub>
</a>
</td>
<td align="center">
<a href="https://github.com/PaoComPlanta">
<img src="https://github.com/PaoComPlanta.png" width="100px;" alt="PaoComPlanta"/><br />
<sub><b>PaoComPlanta</b></sub>
</a>
</td>
<td align="center">
<a href="https://github.com/Vitor-Senra">
<img src="https://github.com/Vitor-Senra.png" width="100px;" alt="Vitor-Senra"/><br />
<sub><b>Vitor-Senra</b></sub>
</a>
</td>
</a>
</td>
</tr>
</table>

---

<div align="center">

**Developed for the Computer Graphics Course**

*Universidade do Minho*

</div>