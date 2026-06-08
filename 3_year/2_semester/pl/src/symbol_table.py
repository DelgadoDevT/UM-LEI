"""
Symbol table implementation for the Fortran compiler.

This module provides the `SymbolTable` class which manages variable
declarations, variable scopes, and subprogram signatures during
semantic analysis and code generation.
"""
from typing import Any, Optional

class SemanticError(Exception):
    """
    Exception raised for logic and typing errors in the source code.
    """
    pass

class SymbolTable:
    """
    Manages scopes, variable declarations, and subprogram signatures.

    The symbol table is structured as a stack of dictionaries to support
    nested scopes. It also maintains a dictionary of subprogram (functions
    and subroutines) signatures globally.

    Attributes:
        scopes (list[dict[str, dict[str, Any]]]): Stack of scopes representing
            the nested symbol tables.
        signatures (dict[str, dict[str, Any]]): Dictionary mapping subprogram
            names to their metadata (kind, arity, return type).
    """

    def __init__(self) -> None:
        """
        Initialize the SymbolTable with a global scope and default built-in functions.
        """
        self.scopes: list[dict[str, dict[str, Any]]] = [{}]
        
        self.signatures: dict[str, dict[str, Any]] = {
            'MOD': {'kind': 'FUNCTION', 'arity': 2, 'return_type': 'INTEGER'},
            'ABS': {'kind': 'FUNCTION', 'arity': 1, 'return_type': 'INTEGER'},
            'SIN': {'kind': 'FUNCTION', 'arity': 1, 'return_type': 'REAL'},
            'COS': {'kind': 'FUNCTION', 'arity': 1, 'return_type': 'REAL'},
            'EXP': {'kind': 'FUNCTION', 'arity': 1, 'return_type': 'REAL'},
            'LOG': {'kind': 'FUNCTION', 'arity': 1, 'return_type': 'REAL'},
            'SQRT': {'kind': 'FUNCTION', 'arity': 1, 'return_type': 'REAL'},
        }

    def enter_scope(self) -> None:
        """
        Enter a new nested scope.
        
        Appends a new empty dictionary to the scope stack.
        """
        self.scopes.append({})

    def exit_scope(self) -> None:
        """
        Exit the current scope.
        
        Pops the top dictionary from the scope stack, unless only the global
        scope remains.
        """
        if len(self.scopes) > 1:
            self.scopes.pop()

    def declare(self, name: str, var_type: str, is_array: bool = False, dimensions: Optional[list[Any]] = None, **extra: Any) -> None:
        """
        Declare a new variable in the current scope.

        Args:
            name (str): The name of the variable to declare.
            var_type (str): The data type of the variable (e.g., 'INTEGER', 'REAL').
            is_array (bool, optional): Indicates if the variable is an array. Defaults to False.
            dimensions (Optional[list[Any]], optional): A list containing the dimensions of the array. Defaults to None.
            **extra (Any): Additional properties to store with the variable.

        Raises:
            SemanticError: If the variable has already been declared in the current scope
                with a known type.
        """
        name_upper = name.upper()
        current_scope = self.scopes[-1]

        if name_upper in current_scope and current_scope[name_upper]['type'] not in ('ANY', 'UNKNOWN'):
            raise SemanticError(f"Variável '{name_upper}' já foi declarada neste escopo.")
            
        current_scope[name_upper] = {
            "type": var_type,
            "is_array": is_array,
            "dimensions": dimensions,
            **extra
        }

    def lookup(self, name: str) -> Optional[dict[str, Any]]:
        """
        Look up a variable by its name across all active scopes.

        Searches from the innermost (current) scope to the outermost (global) scope.

        Args:
            name (str): The name of the variable to search for.

        Returns:
            Optional[dict[str, Any]]: A dictionary of the variable's properties if found,
                otherwise None.
        """
        name_upper = name.upper()
        for scope in reversed(self.scopes):
            if name_upper in scope:
                return scope[name_upper]
        return None

    def is_declared(self, name: str) -> bool:
        """
        Check if a variable is declared in any active scope.

        Args:
            name (str): The name of the variable to check.

        Returns:
            bool: True if the variable is declared, False otherwise.
        """
        return self.lookup(name) is not None

    def declare_subprogram(self, name: str, kind: str, arity: int, return_type: Optional[str] = None) -> None:
        """
        Register a new subprogram (function or subroutine) signature.

        Args:
            name (str): The name of the subprogram.
            kind (str): The kind of the subprogram ('FUNCTION' or 'SUBROUTINE').
            arity (int): The number of arguments the subprogram accepts.
            return_type (Optional[str], optional): The return type if it is a function. Defaults to None.
        """
        name_upper = name.upper()
        self.signatures[name_upper] = {"kind": kind, "arity": arity, "return_type": return_type}

    def get_subprogram(self, name: str) -> Optional[dict[str, Any]]:
        """
        Retrieve a subprogram's signature by its name.

        Args:
            name (str): The name of the subprogram.

        Returns:
            Optional[dict[str, Any]]: A dictionary of the subprogram's signature if found,
                otherwise None.
        """
        return self.signatures.get(name.upper())
