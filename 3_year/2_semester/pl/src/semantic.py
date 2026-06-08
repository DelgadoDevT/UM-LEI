"""Semantic analysis module for the compiler.

This module provides the `SemanticAnalyzer` class, which walks the AST
to perform type checking, variable declarations, and control flow validation.
"""

from typing import Any
from src.symbol_table import SymbolTable, SemanticError
from src.ast_nodes import *

class SemanticAnalyzer:
    """Performs semantic analysis on the AST.

    Validates types, variable declarations, subroutine/function signatures,
    and control flow structures like DO loops and GOTOs.
    """

    def __init__(self) -> None:
        """Initializes the semantic analyzer with an empty symbol table and flow tracking sets."""
        self.symtab = SymbolTable()
        self.defined_labels = set()
        self.label_statement_types = {}
        self.goto_targets = set()
        self.do_targets = set()

    def analyze(self, ast_tree: Any) -> None:
        """Analyzes the given AST tree.
        
        Args:
            ast_tree (Any): The root node of the AST.
            
        Returns:
            None
        """
        self._register_signatures(ast_tree)
        self.visit(ast_tree)

    def _register_signatures(self, nodes: Any) -> None:
        """Registers the signatures of subroutines and functions in the symbol table.
        
        Args:
            nodes (Any): The AST node or list of nodes to process.
            
        Returns:
            None
        """
        if isinstance(nodes, list):
            for n in nodes:
                self._register_signatures(n)
        elif type(nodes).__name__ == 'SubroutineNode':
            self.symtab.declare_subprogram(nodes.name, 'SUBROUTINE', len(nodes.params))
        elif type(nodes).__name__ == 'FunctionNode':
            self.symtab.declare_subprogram(nodes.name, 'FUNCTION', len(nodes.params), return_type=nodes.return_type)

    def visit(self, node: Any) -> Any:
        """Visits an AST node and delegates to the appropriate visit method.
        
        Args:
            node (Any): The AST node to visit.
            
        Returns:
            Any: The result of the specific visitor method.
        """
        if node is None: return None
        
        if isinstance(node, list):
            res = None
            for n in node: res = self.visit(n)
            return res

        if isinstance(node, tuple):
            if not node: return None
            if node[0] == "var_ref":
                return self.visit_Variable(Variable(name=node[1]))
            if node[0] == "var_ref_array":
                return self.visit_ArrayRef({"name": node[1], "indices": node[2]})
            if node[0] == "stop":
                return None
            res = None
            for n in node: res = self.visit(n)
            return res

        if isinstance(node, dict):
            if node.get("node_type") == "array_ref":
                return self.visit_ArrayRef(node)
            elif node.get("node_type") == "function call":
                return self.visit_FunctionCallExpr(node)
            return None

        # Statement label detection
        stmt_label = None
        if hasattr(node, 'stmt_label') and node.stmt_label is not None:
            stmt_label = node.stmt_label
        elif isinstance(node, ContinueNode) and node.label is not None:
            stmt_label = node.label
            
        if stmt_label is not None:
            self.defined_labels.add(stmt_label)
            self.label_statement_types[stmt_label] = type(node).__name__

        method_name = f'visit_{type(node).__name__}'
        visitor = getattr(self, method_name, self.generic_visit)
        return visitor(node)

    def generic_visit(self, node: Any) -> Any:
        """Fallback method for unsupported AST nodes.
        
        Args:
            node (Any): The AST node that lacks a specific visit method.
            
        Raises:
            Exception: Indicates the missing visit method for the given node type.
        """
        raise Exception(f"Motor semântico não encontrou o método: visit_{type(node).__name__}")

    def _verify_and_reset_flow(self) -> None:
        """Verifies that all GOTOs and DO loops target valid labels and resets tracking.
        
        Raises:
            SemanticError: If an unclosed DO loop, invalid DO termination,
                           or undefined GOTO label is found.
                           
        Returns:
            None
        """
        missing_dos = self.do_targets - self.defined_labels
        if missing_dos: 
            raise SemanticError("Unclosed DO loop")

        for label in self.do_targets:
            stmt_type = self.label_statement_types.get(label)
            if stmt_type != 'ContinueNode':
                raise SemanticError(f"DO loop label {label} must terminate with CONTINUE")
            
        missing_gotos = self.goto_targets - self.defined_labels
        if missing_gotos: 
            raise SemanticError("Undefined GOTO label")
        
        self.defined_labels = set()
        self.label_statement_types = {}
        self.goto_targets = set()
        self.do_targets = set()

    def visit_ProgramNode(self, node: ProgramNode) -> None:
        """Visits a ProgramNode, processing its body and verifying control flow.
        
        Args:
            node (ProgramNode): The program node to process.
            
        Returns:
            None
        """
        self.symtab.enter_scope()
        for stmt in node.body: self.visit(stmt)
        self._verify_and_reset_flow()
        self.symtab.exit_scope()

    def visit_SubroutineNode(self, node: SubroutineNode) -> None:
        """Visits a SubroutineNode, declaring parameters and processing the body.
        
        Args:
            node (SubroutineNode): The subroutine node to process.
            
        Returns:
            None
        """
        self.symtab.enter_scope()
        for param in node.params: self.symtab.declare(param, "ANY")
        for stmt in node.body: self.visit(stmt)
        self._verify_and_reset_flow()
        self.symtab.exit_scope()

    def visit_FunctionNode(self, node: FunctionNode) -> None:
        """Visits a FunctionNode, declaring parameters, return type, and processing the body.
        
        Args:
            node (FunctionNode): The function node to process.
            
        Returns:
            None
        """
        self.symtab.enter_scope()
        implicit_rt = "INTEGER" if node.name.upper()[0] in 'IJKLMN' else "REAL"
        self.symtab.declare(node.name, node.return_type or implicit_rt)
        for param in node.params: self.symtab.declare(param, "ANY")
        for stmt in node.body: self.visit(stmt)
        self._verify_and_reset_flow()
        self.symtab.exit_scope()

    def visit_VarDecl(self, node: VarDecl) -> None:
        """Visits a VarDecl node to declare a variable or array in the symbol table.
        
        Args:
            node (VarDecl): The variable declaration node to process.
            
        Returns:
            None
        """
        is_array = (node.dimensions is not None) and (len(node.dimensions) > 0)
        is_dimension = node.var_type == "DIMENSION" or isinstance(node.var_type, list)
        existing = self.symtab.lookup(node.name)
        
        if is_dimension:
            if existing:
                existing['is_array'] = True
                existing['dimensions'] = node.dimensions
            else:
                self.symtab.declare(node.name, "UNKNOWN", is_array=True, dimensions=node.dimensions)
        else:
            if existing and existing['type'] in ('ANY', 'UNKNOWN'):
                existing['type'] = node.var_type
                if is_array:
                    existing['is_array'] = True
                    existing['dimensions'] = node.dimensions
            else:
                self.symtab.declare(node.name, node.var_type, is_array=is_array, dimensions=node.dimensions)

    def visit_Variable(self, node: Variable) -> str:
        """Visits a Variable node, declaring it implicitly if it doesn't exist.
        
        Args:
            node (Variable): The variable node to process.
            
        Returns:
            str: The type of the variable.
        """
        var_info = self.symtab.lookup(node.name)
        if not var_info:
            implicit_type = 'INTEGER' if node.name.upper()[0] in 'IJKLMN' else 'REAL'
            self.symtab.declare(node.name, implicit_type)
            return implicit_type
        return var_info['type']

    def visit_ArrayRef(self, node_dict: dict) -> str:
        """Visits an array reference dictionary, verifying its validity and dimensions.
        
        Args:
            node_dict (dict): The dictionary containing array reference info.
            
        Raises:
            SemanticError: If the array is undeclared, not an array, or has non-numeric indices.
            
        Returns:
            str: The type of the array elements.
        """
        var_info = self.symtab.lookup(node_dict['name'])
        if not var_info: raise SemanticError(f"O array '{node_dict['name']}' não foi declarado.")
        if not var_info.get('is_array'): raise SemanticError(f"A variável '{node_dict['name']}' não é um Array.")
        for idx_expr in node_dict['indices']:
            idx_type = self.visit(idx_expr)
            if idx_type not in ['INTEGER', 'REAL']: 
                raise SemanticError(f"Índice do array '{node_dict['name']}' tem de ser numérico.")
        return var_info['type']

    def visit_Literal(self, node: Literal) -> str:
        """Visits a Literal node to extract its type.
        
        Args:
            node (Literal): The literal node.
            
        Returns:
            str: The type of the literal.
        """
        return node.type_name

    def visit_UnOp(self, node: UnOp) -> str:
        """Visits a UnOp node and verifies its operand type.
        
        Args:
            node (UnOp): The unary operation node.
            
        Raises:
            SemanticError: If the operation is invalid for the operand type.
            
        Returns:
            str: The result type of the unary operation.
        """
        op_type = self.visit(node.operand)
        if node.op == 'NOT' and op_type != 'LOGICAL':
            raise SemanticError(f"Operador NOT exige LOGICAL.")
        if node.op in ['+', '-'] and op_type not in ['INTEGER', 'REAL']:
            raise SemanticError(f"Operador Unário exige numérico.")
        return op_type

    def visit_BinOp(self, node: BinOp) -> str:
        """Visits a BinOp node and checks compatibility between operands.
        
        Args:
            node (BinOp): The binary operation node.
            
        Raises:
            SemanticError: If the operands have incompatible types for the operation.
            
        Returns:
            str: The resulting type of the binary operation.
        """
        left_type = self.visit(node.left)
        right_type = self.visit(node.right)
        op = node.op

        if op == '=':
            if left_type == 'LOGICAL' and right_type != 'LOGICAL': raise SemanticError("Atribuição inválida.")
            if left_type != 'LOGICAL' and right_type == 'LOGICAL': raise SemanticError("Atribuição inválida.")
            return left_type
        if op in ['+', '-', '*', '/', '**']:
            if left_type not in ['INTEGER', 'REAL'] or right_type not in ['INTEGER', 'REAL']: raise SemanticError(f"Operador '{op}' inválido.")
            return 'REAL' if 'REAL' in [left_type, right_type] else 'INTEGER'
        if op in ['.AND.', '.OR.', '.EQV.', '.NEQV.']:
            if left_type != 'LOGICAL' or right_type != 'LOGICAL': raise SemanticError("Operadores lógicos exigem LOGICAL.")
            return 'LOGICAL'
        if op in ['.EQ.', '.NE.', '.LT.', '.LE.', '.GT.', '.GE.']:
            if left_type == 'LOGICAL' or right_type == 'LOGICAL': raise SemanticError("Operador relacional não suporta lógicos.")
            return 'LOGICAL'
        if op == '//':
            if left_type != 'CHARACTER' or right_type != 'CHARACTER': raise SemanticError("Operador '//' exige CHARACTER.")
            return 'CHARACTER'
        return "UNKNOWN"

    def visit_IfNode(self, node: IfNode) -> None:
        """Visits an IfNode and validates its condition.
        
        Args:
            node (IfNode): The if statement node.
            
        Raises:
            SemanticError: If the condition is not of LOGICAL type.
            
        Returns:
            None
        """
        if self.visit(node.cond) != 'LOGICAL':
            raise SemanticError("Condição do IF exige LOGICAL")
        for stmt in node.then_block: self.visit(stmt)
        for stmt in node.else_block: self.visit(stmt)

    def visit_DoNode(self, node: DoNode) -> None:
        """Visits a DoNode, verifying its control variable and bounds.
        
        Args:
            node (DoNode): The do loop node.
            
        Raises:
            SemanticError: If the control variable is not numeric.
            
        Returns:
            None
        """
        self.visit(node.start)
        self.visit(node.end)
        if node.step: self.visit(node.step)
        
        var_type = self.visit_Variable(Variable(name=node.var))
        if var_type not in ['INTEGER', 'REAL']:
            raise SemanticError("Variável do DO tem de ser numérica.")
            
        self.do_targets.add(node.label)
        for stmt in node.block: self.visit(stmt)

    def visit_GotoNode(self, node: GotoNode) -> None:
        """Visits a GotoNode, tracking the target label.
        
        Args:
            node (GotoNode): The goto statement node.
            
        Returns:
            None
        """
        self.goto_targets.add(node.label)

    def visit_ContinueNode(self, node: ContinueNode) -> None:
        """Visits a ContinueNode.
        
        Args:
            node (ContinueNode): The continue statement node.
            
        Returns:
            None
        """
        pass

    def visit_CallNode(self, node: CallNode) -> None:
        """Visits a CallNode, verifying subroutine existence and argument count.
        
        Args:
            node (CallNode): The call statement node.
            
        Raises:
            SemanticError: If the subprogram is not a subroutine or arity mismatches.
            
        Returns:
            None
        """
        sig = self.symtab.get_subprogram(node.subroutine_name)
        if sig:
            if sig['kind'] != 'SUBROUTINE': raise SemanticError(f"Elemento não é SUBROUTINE.")
            if len(node.args) != sig['arity']:
                raise SemanticError(f"expects {sig['arity']} arguments but got {len(node.args)}")
        for arg in node.args: self.visit(arg)

    def visit_FunctionCallExpr(self, node_dict: dict) -> str:
        """Visits a function call expression, resolving it to an array ref if needed.
        
        Args:
            node_dict (dict): The function call expression dictionary.
            
        Raises:
            SemanticError: If the subprogram is not a function or arity mismatches.
            
        Returns:
            str: The return type of the function or array type.
        """
        name = node_dict['name'].upper()
        sig = self.symtab.get_subprogram(name)
        if sig:
            if sig['kind'] != 'FUNCTION': raise SemanticError(f"Elemento não é FUNCTION.")
            if len(node_dict['args']) != sig['arity']: raise SemanticError(f"Função espera {sig['arity']} args.")
            for arg in node_dict['args']: self.visit(arg)
            if sig.get('return_type') is not None:
                return sig['return_type']
            return 'INTEGER' if name[0] in 'IJKLMN' else 'REAL'

        var_info = self.symtab.lookup(name)
        if var_info:
            if var_info.get("is_array"):
                node_dict["node_type"] = "array_ref"
                node_dict["indices"] = node_dict["args"]
                del node_dict["args"]
            for arg in node_dict.get('args', node_dict.get('indices', [])): self.visit(arg)
            return var_info['type']
            
        for arg in node_dict['args']: self.visit(arg)
        return 'INTEGER' if name[0] in 'IJKLMN' else 'REAL'

    def visit_PrintNode(self, node: PrintNode) -> None:
        """Visits a PrintNode and processes its expressions.
        
        Args:
            node (PrintNode): The print statement node.
            
        Returns:
            None
        """
        for expr in node.expressions: self.visit(expr)

    def visit_ReadNode(self, node: ReadNode) -> None:
        """Visits a ReadNode and handles variables read.
        
        Args:
            node (ReadNode): The read statement node.
            
        Returns:
            None
        """
        for var in node.variables:
            if isinstance(var, tuple) and var[0] == "var_ref":
                if not self.symtab.is_declared(var[1]):
                    self.visit_Variable(Variable(name=var[1]))
            else:
                self.visit(var)

    def visit_ReturnNode(self, node: ReturnNode) -> None:
        """Visits a ReturnNode.
        
        Args:
            node (ReturnNode): The return statement node.
            
        Returns:
            None
        """
        pass
