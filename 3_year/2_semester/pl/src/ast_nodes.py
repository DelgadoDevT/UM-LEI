"""
Abstract Syntax Tree (AST) node definitions for the Fortran compiler.

This module defines the classes used to represent different constructs
of the Fortran language in the AST. Each class corresponds to a specific
node type, such as operators, literals, variables, and control structures.
"""
from __future__ import annotations

from typing import Any, Optional


class Node:
    """
    Base class for all Abstract Syntax Tree (AST) nodes.
    """
    pass


class BinOp(Node):
    """
    Represents a binary operation in the AST.

    Attributes:
        left (Any): The left operand.
        op (str): The operator (e.g., '+', '-', '*', '/').
        right (Any): The right operand.
    """
    def __init__(self, left: Any, op: str, right: Any) -> None:
        """
        Initialize a binary operation node.

        Args:
            left (Any): The left operand.
            op (str): The binary operator as a string.
            right (Any): The right operand.
        """
        self.left = left
        self.op = op
        self.right = right


class UnOp(Node):
    """
    Represents a unary operation in the AST.

    Attributes:
        op (str): The operator (e.g., '+', '-').
        operand (Any): The operand to which the operator is applied.
    """
    def __init__(self, op: str, operand: Any) -> None:
        """
        Initialize a unary operation node.

        Args:
            op (str): The unary operator as a string.
            operand (Any): The operand.
        """
        self.op = op
        self.operand = operand


class Literal(Node):
    """
    Represents a literal value (e.g., integer or real number) in the AST.

    Attributes:
        value (Any): The literal value.
        type_name (str): The type of the literal (e.g., 'INTEGER', 'REAL').
    """
    def __init__(self, value: Any, type_name: str) -> None:
        """
        Initialize a literal node.

        Args:
            value (Any): The literal value.
            type_name (str): The type of the literal.
        """
        self.value = value
        self.type_name = type_name


class Variable(Node):
    """
    Represents a variable access in the AST.

    Attributes:
        name (str): The name of the variable.
    """
    def __init__(self, name: str) -> None:
        """
        Initialize a variable node.

        Args:
            name (str): The variable's name.
        """
        self.name = name


class VarDecl(Node):
    """
    Represents a variable declaration in the AST.

    Attributes:
        name (str): The name of the variable being declared.
        var_type (str): The data type of the variable.
        dimensions (Optional[list[Any]]): The dimensions if it's an array, else None.
    """
    def __init__(self, name: str, var_type: str, dimensions: Optional[list[Any]] = None) -> None:
        """
        Initialize a variable declaration node.

        Args:
            name (str): The variable name.
            var_type (str): The variable's data type.
            dimensions (Optional[list[Any]], optional): A list containing dimensions for an array declaration. Defaults to None.
        """
        self.name = name
        self.var_type = var_type
        self.dimensions = dimensions


# ----- Simão: estrutura, fluxo e I/O -----
class ProgramNode(Node):
    """
    Represents the main program structure in the AST.

    Attributes:
        name (str): The name of the program.
        body (list[Any]): A list of nodes representing the program's statements.
    """
    def __init__(self, name: str, body: list[Any]) -> None:
        """
        Initialize a program node.

        Args:
            name (str): The program name.
            body (list[Any]): The statements comprising the program body.
        """
        self.name = name
        self.body = body


class FunctionNode(Node):
    """
    Represents a function definition in the AST.

    Attributes:
        name (str): The name of the function.
        return_type (Optional[str]): The return type of the function.
        params (list[str]): A list of parameter names.
        body (list[Any]): The statements comprising the function's body.
    """
    def __init__(self, name: str, return_type: Optional[str], params: list[str], body: list[Any]) -> None:
        """
        Initialize a function node.

        Args:
            name (str): The function name.
            return_type (Optional[str]): The return data type.
            params (list[str]): A list of parameter names.
            body (list[Any]): The statements in the function body.
        """
        self.name = name
        self.return_type = return_type
        self.params = params
        self.body = body


class SubroutineNode(Node):
    """
    Represents a subroutine definition in the AST.

    Attributes:
        name (str): The name of the subroutine.
        params (list[str]): A list of parameter names.
        body (list[Any]): The statements comprising the subroutine's body.
    """
    def __init__(self, name: str, params: list[str], body: list[Any]) -> None:
        """
        Initialize a subroutine node.

        Args:
            name (str): The subroutine name.
            params (list[str]): A list of parameter names.
            body (list[Any]): The statements in the subroutine body.
        """
        self.name = name
        self.params = params
        self.body = body


class IfNode(Node):
    """
    Represents an IF-THEN-ELSE control structure in the AST.

    Attributes:
        cond (Any): The condition expression to evaluate.
        then_block (list[Any]): The statements to execute if the condition is true.
        else_block (list[Any]): The statements to execute if the condition is false.
    """
    def __init__(self, cond: Any, then_block: list[Any], else_block: list[Any]) -> None:
        """
        Initialize an IF control structure node.

        Args:
            cond (Any): The conditional expression node.
            then_block (list[Any]): The list of statements for the THEN block.
            else_block (list[Any]): The list of statements for the ELSE block.
        """
        self.cond = cond
        self.then_block = then_block
        self.else_block = else_block


class DoNode(Node):
    """
    Represents a DO loop control structure in the AST.

    Attributes:
        label (int): The loop's ending label.
        var (str): The loop counter variable.
        start (Any): The starting value expression.
        end (Any): The ending value expression.
        step (Optional[Any]): The step value expression.
        block (list[Any]): The statements comprising the loop body.
    """
    def __init__(
        self,
        label: int,
        var: str,
        start: Any,
        end: Any,
        step: Optional[Any],
        block: list[Any],
    ) -> None:
        """
        Initialize a DO loop node.

        Args:
            label (int): The ending label for the loop.
            var (str): The name of the loop counter variable.
            start (Any): The starting value expression node.
            end (Any): The ending value expression node.
            step (Optional[Any]): The step value expression node.
            block (list[Any]): The list of statements in the loop body.
        """
        self.label = label
        self.var = var
        self.start = start
        self.end = end
        self.step = step
        self.block = block


class GotoNode(Node):
    """
    Represents a GOTO statement in the AST.

    Attributes:
        label (int): The target label to jump to.
    """
    def __init__(self, label: int) -> None:
        """
        Initialize a GOTO statement node.

        Args:
            label (int): The integer label marking the jump target.
        """
        self.label = label


class ContinueNode(Node):
    """
    Represents a CONTINUE statement in the AST.

    Attributes:
        label (Optional[int]): The optional label associated with this statement.
    """
    def __init__(self, label: Optional[int] = None) -> None:
        """
        Initialize a CONTINUE statement node.

        Args:
            label (Optional[int], optional): The label for this statement. Defaults to None.
        """
        self.label = label


class PrintNode(Node):
    """
    Represents a PRINT statement in the AST.

    Attributes:
        expressions (list[Any]): The list of expressions to be printed.
    """
    def __init__(self, expressions: list[Any]) -> None:
        """
        Initialize a PRINT statement node.

        Args:
            expressions (list[Any]): The expressions to evaluate and print.
        """
        self.expressions = expressions


class ReadNode(Node):
    """
    Represents a READ statement in the AST.

    Attributes:
        variables (list[Any]): The list of variables to store the read values.
    """
    def __init__(self, variables: list[Any]) -> None:
        """
        Initialize a READ statement node.

        Args:
            variables (list[Any]): The variables to read into.
        """
        self.variables = variables


class CallNode(Node):
    """
    Represents a CALL statement to a subroutine in the AST.

    Attributes:
        subroutine_name (str): The name of the subroutine being called.
        args (list[Any]): The list of arguments passed to the subroutine.
    """
    def __init__(self, subroutine_name: str, args: list[Any]) -> None:
        """
        Initialize a CALL statement node.

        Args:
            subroutine_name (str): The subroutine name.
            args (list[Any]): The list of argument expressions.
        """
        self.subroutine_name = subroutine_name
        self.args = args


class ReturnNode(Node):
    """
    Represents a RETURN statement in the AST.
    """
    def __init__(self) -> None:
        """
        Initialize a RETURN statement node.
        """
        pass
