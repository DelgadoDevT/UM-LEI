# Program Design by Calculation

This folder contains the practical assignment developed for the **Program Design by Calculation** (Cálculo de Programas) course at Universidade do Minho.

## Overview

The course treats computer programming as a scientific discipline, utilizing the "Algebra of Programming" to derive programs from specifications. The project is written in **Literate Haskell**, combining executable code with mathematical documentation.

The work addresses four main problems:
* **Breadth-First Search (BFS):** Implementing a level-order tree traversal using a catamorphism to stratify the tree and an anamorphism using a queue-based state.
* **Taylor Series (sinh x):** Deriving a mutually recursive function to calculate hyperbolic sine series approximations efficiently, using finite differences to update polynomial coefficients.
* **Infinite Streams (Fair Merge):** Implementing a "fair merge" algorithm for infinite sequences to ensure no stream is starved, derived via Fokkinga's dual law of mutual recursion.
* **Probabilistic Telegraph:** Modeling a faulty communication system using a Probabilistic Monad and a custom probabilistic catamorphism to analyze transmission success rates.

## How to Run

The project is designed to run inside a Docker container to ensure all dependencies (Haskell GHC, LaTeX, lhs2TeX) are correctly configured.

### 1. Docker Setup
Build the Docker image and run the container, mounting the current directory to share files between your host and the container:

```bash
docker build -t cp2526t .
docker run -v ${PWD}:/cp2526t -it cp2526t
```

### 2. Executing the Code
The file cp2526t.lhs is an executable Haskell module. You can load it into the GHC interpreter to run functions and test cases:

```bash
ghci cp2526t.lhs
```

### 3. Generating the PDF Report
To generate the full documentation (PDF) from the Literate Haskell source, you can use the provided Makefile or run the commands manually inside the container.

**Using Make:**
```bash
make full
```

**Manual Compilation:**
```bash
# Pre-process Literate Haskell to LaTeX
lhs2TeX cp2526t.lhs > cp2526t.tex

# Compile PDF and generate auxiliary indices
pdflatex cp2526t
bibtex cp2526t.aux
makeindex cp2526t.idx
pdflatex cp2526t
```

## Acknowledgments

Special thanks to **José Nuno Oliveira** and the teaching staff for providing the necessary support libraries—including `Cp`, `BTree`, and `Probability`—used throughout this project.

## Authors

<table> <tr> <td align="center"><a href="https://github.com/josedasilva11"><img src="https://www.google.com/search?q=https://github.com/josedasilva11.png" width="100px;" alt=""/><br /><sub><b>José Silva</b></sub></a><br /></td> <td align="center"><a href="https://github.com/DelgadoDevT"><img src="https://www.google.com/search?q=https://github.com/DelgadoDevT.png" width="100px;" alt=""/><br /><sub><b>João Delgado</b></sub></a><br /></td> <td align="center"><a href="https://github.com/PaoComPlanta"><img src="https://www.google.com/search?q=https://github.com/PaoComPlanta.png" width="100px;" alt=""/><br /><sub><b>Simão Mendes</b></sub></a><br /></td> </tr> </table>