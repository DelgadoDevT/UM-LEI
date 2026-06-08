# 💻 Fortran 77 Compiler

![Python](https://img.shields.io/badge/Python-%E2%89%A5%203.14-blue?logo=python&logoColor=white)
![PLY](https://img.shields.io/badge/PLY-%E2%89%A5%203.11-green)
![Poetry](https://img.shields.io/badge/Poetry-Package_Manager-cyan?logo=poetry&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow)

Project developed within the scope of the **Language Processing** course (3rd year, 2nd semester, 2025/2026) of the Bachelor's degree in Informatics Engineering at the **University of Minho**.

The main objective is the development of a robust compiler capable of processing source code written in the **Fortran 77** language (ANSI X3.9-1978 standard, with a fixed 72-column format) and translating it fully into executable *bytecode* for the **EWVM** Virtual Machine.

## ⚙️ Architecture and Compilation Pipeline

The compiler operates through a well-defined sequential pipeline, encompassing everything from text analysis to final code generation, incorporating an intermediate fixed-point optimization phase:

```text
[ Source Code ] (Fortran 77)
       │
       ▼
╭───────────────╮
│     Lexer     │── Token extraction, fixed format (72 col.), continuation lines
╰───────────────╯
       │
       ▼
╭───────────────╮
│    Parser     │── LALR grammar, AST construction, support for subprograms and arrays
╰───────────────╯
       │
       ▼
╭───────────────╮
│   Semantics   │── Type checking, scope management, and Symbol Tables
╰───────────────╯
       │
       ▼
╭───────────────╮
│ Optimization  │── Constant folding, algebraic simplifications, dead code elimination
╰───────────────╯
       │
       ▼
╭───────────────╮
│   Code Gen    │── EWVM instruction generation, stack management, 1-based indexing
╰───────────────╯
       │
       ▼
[ EWVM Bytecode ]
```

## 📊 Project Status

The current version (`dev` branch) is fully functional. All proposed phases have been successfully completed:

- [x] **Lexical Analysis**
- [x] **Syntactic Analysis (AST)**
- [x] **Semantic Analysis and Symbol Tables**
- [x] **Code Optimization**
- [x] **EWVM Code Generation**

## 🗂️ Repository Structure

The project is organized as follows:

- **`src/`** — Main compiler modules.
  - `lexer.py`: Lexical analysis.
  - `parser.py`: LALR grammar (PLY) and Abstract Syntax Tree (AST) construction.
  - `ast_nodes.py`: Definition of AST data structures (nodes).
  - `semantic.py`: Semantic and type checking.
  - `symbol_table.py`: Symbol management logic divided by scopes.
  - `optimizer.py`: Advanced optimization pipeline.
  - `codegen.py`: Responsible for emitting final instructions for the EWVM.
  - `main.py`: Compiler execution CLI.
- **`tests/`** — Test cases and validation.
  - `exemplo1_ola.f` to `exemplo8_arrays.f`: Demonstration Fortran programs.
  - `test.py`: Automation script to run and assess test results.
  - `expected_outputs/`: Directory containing reference bytecodes for comparison.
- **`docs/`** — Project documentation.
  - `grammar.txt`: Formal specification of the adopted grammar.
  - `optimization.md`: Details about the rules and approach of the optimization phase.

## 📦 Installation and Usage

The project uses **Poetry** for easy and isolated dependency management.

```bash
# 1. Install project dependencies
poetry install

# 2. Compile a specific Fortran file
poetry run python src/main.py tests/exemplo2_fatorial.f
```

## ⚡ Quick Example

Here is a concise example of the transformation performed by the compiler:

**Input Fortran 77 code (`exemplo.f`):**
```fortran
      PROGRAM OLA
      PRINT *, 'HELLO WORLD'
      END
```

**Corresponding generated EWVM Bytecode:**
```text
START
PUSHS "HELLO WORLD"
WRITES
WRITELN
STOP
```

## 🧪 Testing

Our test suite includes 8 carefully designed programs to test various compiler features: arithmetic operations, strings, loops, arrays, and subroutines.

To run the entire test battery automatically:

```bash
poetry run python tests/test.py
```

## 📚 Documentation

The project includes detailed documentation automatically generated via [Sphinx](https://www.sphinx-doc.org/). To compile and consult the documentation locally, follow these steps:

```bash
# 1. Generate documentation in HTML format
poetry run sphinx-build -b html docs docs/_build/html

# 2. Start a local web server to view the documentation
python3 -m http.server -d docs/_build/html 8000
```

After starting the server, open your browser at: [http://localhost:8000](http://localhost:8000)

## 👥 Contributors

This project was developed with the dedication of the following contributors:

- [DelgadoDevT (DelgadoDevT)](https://github.com/DelgadoDevT)
- [Jorge Barbosa](https://github.com/a106799)
- [Simão Mendes (PaoComPlanta)](https://github.com/PaoComPlanta)

## 📄 License

This project is licensed under the **MIT** license. For more details, see the `LICENSE` file included in this repository.
