# Computer Science Labs III project

## Overview

This project involves the development of a music streaming system, where data related to songs, artists, users, and system usage statistics is managed. The system loads this data into appropriate memory structures and uses it to respond to various queries. The project emphasises modular programming in C, with a focus on separating code into interface (`.h`) and implementation (`.c`) components, while also incorporating tools for debugging and memory usage analysis.

## Contributors

* [PaoComPlanta](https://github.com/paocomplanta)
* [ItzPedrOfficial](https://github.com/ItzPedrOfficial)

## Executables

You can compile the program using the following command in the "trabalho-pratico" folder:

```bash
make
```

This will create three executables, each serving a different purpose:

1. **programa-principal**  
   This executable runs the program with a folder containing all five datasets and a `.txt` file with the queries, saving the results to the "resultados" folder:

   ```bash
   ./programa-principal <dataset-path> <queries-input-path>
   ```

2. **programa-testes**  
   This is similar to `programa-principal`, but it accepts an additional parameter: the folder with pre-solved queries. This mode compares the results and generates a file in the "resultados" folder containing the comparison, along with other parameters like time and memory usage:

   ```bash
   ./programa-testes <dataset-path> <queries-input-path> <output-folder-path>
   ```

3. **programa-interactivo**  
   This executable offers the same functionality as `programa-principal` but in an interactive mode, allowing queries to be executed individually:

   ```bash
   ./programa-interactivo
   ```

To clean all generated files, use the following command:

```bash
make clean
```
## Documentation

The documentation was made in Doxygen and can be generated with the command:
```bash
make doc
```
It can be removed with:
```bash
make docclean
```
