"""
Main entry point for the Fortran-to-EWVM compiler.

This module orchestrates the compilation pipeline: parsing, semantic analysis,
optimization, and code generation. It reads a source `.f` file and produces
a compiled `.vm` file.
"""
import os
import sys

# Ajustar PYTHONPATH se for executado diretamente
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from src.lexer import tokenize
from src.parser import parse
from src.semantic import SemanticAnalyzer
from src.optimizer import optimize_ast
from src.codegen import CodeGenerator

def main():
    """
    Execute the compiler pipeline on a given source file.

    The function reads the input file path from command line arguments,
    reads the source code, and processes it through the following stages:
    1. Parsing: Converts source code into an Abstract Syntax Tree (AST).
    2. Semantic Analysis: Checks for semantic errors and builds the symbol table.
    3. Optimization: Simplifies and optimizes the AST.
    4. Code Generation: Generates EWVM byte code from the optimized AST.
    
    The resulting EWVM code is written to a `.vm` file with the same base name.

    Raises:
        SystemExit: If the input file is not provided, does not exist, or if
            a compilation error occurs.
    """
    if len(sys.argv) < 2:
        print("Usage: poetry run python src/main.py <file.f>")
        sys.exit(1)

    filepath = sys.argv[1]
    if not os.path.exists(filepath):
        print(f"Error: File '{filepath}' not found.")
        sys.exit(1)

    try:
        with open(filepath, "r", encoding="utf-8") as f:
            source_code = f.read()

        # 1. Parsing
        ast_tree = parse(source_code)
        
        # 2. Semantic Analysis
        analyzer = SemanticAnalyzer()
        analyzer.analyze(ast_tree)
        symtab = analyzer.symtab
        
        # 3. Optimization
        optimized_ast, _ = optimize_ast(ast_tree)
        
        # 4. Code Generation
        generator = CodeGenerator(symtab)
        vm_code = generator.generate(optimized_ast)
        
        # Output
        out_filepath = filepath.replace('.f', '.vm')
        if out_filepath == filepath:
            out_filepath += '.vm'

        with open(out_filepath, "w", encoding="utf-8") as f_out:
            f_out.write(vm_code)

        print(f"Success! Generated EWVM code at: {out_filepath}")

    except Exception as e:
        print(f"Compilation Error: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
